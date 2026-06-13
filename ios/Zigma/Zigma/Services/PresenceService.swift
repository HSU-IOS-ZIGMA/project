import Foundation

@MainActor
final class PresenceViewModel: ObservableObject {
    @Published private(set) var members: [PresenceMember] = []
    @Published private(set) var modeLabel = "오프라인"

    private var task: Task<Void, Never>?
    private var stompClient: STOMPPresenceClient?
    private var connectionKey: String?

    func startOffline() {
        task?.cancel()
        stompClient?.disconnect()
        task = nil
        stompClient = nil
        connectionKey = nil
        members = []
        modeLabel = "오프라인"
    }

    func startLive(promiseId: Int, accessToken: String, forceReconnect: Bool = false) {
        let nextConnectionKey = "\(promiseId):\(accessToken)"
        if !forceReconnect, connectionKey == nextConnectionKey, task != nil {
            return
        }

        task?.cancel()
        stompClient?.disconnect()
        connectionKey = nextConnectionKey
        members = []
        modeLabel = "접속 중"

        let client = STOMPPresenceClient(
            url: AppConfig.webSocketURL,
            promiseId: promiseId,
            accessToken: accessToken
        )
        stompClient = client

        task = Task { [weak self] in
            do {
                for try await members in client.connect() {
                    self?.members = members
                    self?.modeLabel = "실시간"
                }
            } catch {
                self?.members = []
                self?.modeLabel = "연결 실패"
            }
        }
    }

    func stop() {
        task?.cancel()
        task = nil
        stompClient?.disconnect()
        stompClient = nil
        connectionKey = nil
        modeLabel = "오프라인"
    }
}

final class STOMPPresenceClient {
    private let url: URL
    private let promiseId: Int
    private let accessToken: String
    private var webSocketTask: URLSessionWebSocketTask?
    private var isConnected = false

    init(url: URL, promiseId: Int, accessToken: String) {
        self.url = url
        self.promiseId = promiseId
        self.accessToken = accessToken
    }

    func connect() -> AsyncThrowingStream<[PresenceMember], Error> {
        AsyncThrowingStream { continuation in
            let task = URLSession.shared.webSocketTask(with: url)
            webSocketTask = task
            task.resume()

            Task {
                do {
                    try await send(command: "CONNECT", headers: [
                        "accept-version": "1.2",
                        "heart-beat": "10000,10000",
                        "Authorization": "Bearer \(accessToken)"
                    ])

                    while !Task.isCancelled {
                        let message = try await task.receive()
                        guard case .string(let payload) = message else { continue }

                        for frame in Self.frames(from: payload) {
                            let parsedFrame = Self.parse(frame)

                            switch parsedFrame.command {
                            case "CONNECTED":
                                isConnected = true
                                try await subscribeAndJoin()
                            case "MESSAGE":
                                guard let data = parsedFrame.body.data(using: .utf8) else { continue }
                                let members = try JSONDecoder.zigma.decode([PresenceMember].self, from: data)
                                continuation.yield(members)
                            case "ERROR":
                                throw PresenceSocketError.stompError(parsedFrame.body)
                            default:
                                continue
                            }
                        }
                    }
                } catch {
                    continuation.finish(throwing: error)
                }
            }
        }
    }

    func disconnect() {
        Task {
            if isConnected {
                try? await send(command: "SEND", headers: [
                    "destination": "/app/promises/\(promiseId)/presence/leave"
                ])
                try? await send(command: "DISCONNECT", headers: [:])
            }
            isConnected = false
            webSocketTask?.cancel(with: .normalClosure, reason: nil)
            webSocketTask = nil
        }
    }

    private func subscribeAndJoin() async throws {
        try await send(command: "SUBSCRIBE", headers: [
            "id": "presence-\(promiseId)",
            "destination": "/topic/promises/\(promiseId)/presence"
        ])
        try await send(command: "SEND", headers: [
            "destination": "/app/promises/\(promiseId)/presence/join"
        ])
    }

    private func send(command: String, headers: [String: String]) async throws {
        var frame = command + "\n"
        for (key, value) in headers {
            frame += "\(key):\(value)\n"
        }
        frame += "\n\u{0}"
        try await webSocketTask?.send(.string(frame))
    }

    private static func frames(from payload: String) -> [String] {
        payload
            .split(separator: "\u{0}", omittingEmptySubsequences: true)
            .map { String($0).trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
    }

    private static func parse(_ frame: String) -> STOMPFrame {
        let components = frame.components(separatedBy: "\n\n")
        let headerBlock = components.first ?? ""
        let body = components.dropFirst().joined(separator: "\n\n")
        let lines = headerBlock.split(separator: "\n", omittingEmptySubsequences: true)
        let command = lines.first.map(String.init) ?? ""

        return STOMPFrame(command: command, body: body)
    }
}

private struct STOMPFrame {
    let command: String
    let body: String
}

private enum PresenceSocketError: Error {
    case stompError(String)
}
