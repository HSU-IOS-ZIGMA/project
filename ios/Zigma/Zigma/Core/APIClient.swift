import Foundation

enum APIError: Error, LocalizedError {
    case invalidResponse
    case unauthorized(message: String)
    case conflict(message: String)
    case server(message: String)

    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "서버 응답을 읽을 수 없습니다."
        case .unauthorized(let message):
            return message
        case .conflict(let message):
            return message
        case .server(let message):
            return message
        }
    }
}

struct APIClient {
    var accessToken: String?
    var baseURL: URL = AppConfig.apiBaseURL

    func request<T: Decodable>(
        _ path: String,
        method: String = "GET",
        body: Encodable? = nil,
        queryItems: [URLQueryItem] = []
    ) async throws -> T {
        var components = URLComponents(url: baseURL.appendingPathComponent(path), resolvingAgainstBaseURL: false)
        if !queryItems.isEmpty {
            components?.queryItems = queryItems
        }

        guard let url = components?.url else {
            throw APIError.invalidResponse
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let accessToken {
            request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
            print("[Zigma API] \(method) \(url.absoluteString) token=\(String(accessToken.prefix(18)))...")
        } else {
            print("[Zigma API] \(method) \(url.absoluteString) token=nil")
        }

        if let body {
            request.httpBody = try JSONEncoder.zigma.encode(AnyEncodable(body))
        }

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }

        if httpResponse.statusCode == 401 {
            let message = APIClient.errorMessage(from: data, fallback: "인증이 필요합니다.")
            throw APIError.unauthorized(message: message)
        }

        if httpResponse.statusCode == 409 {
            let message = APIClient.errorMessage(from: data, fallback: "이미 처리된 투표입니다.")
            throw APIError.conflict(message: message)
        }

        if !(200..<300).contains(httpResponse.statusCode) {
            let message = APIClient.errorMessage(from: data, fallback: "요청에 실패했습니다.")
            throw APIError.server(message: message)
        }

        return try JSONDecoder.zigma.decode(T.self, from: data)
    }

    private static func errorMessage(from data: Data, fallback: String) -> String {
        if let payload = try? JSONDecoder.zigma.decode(APIErrorEnvelope.self, from: data),
           let message = payload.message,
           !message.isEmpty {
            return message
        }

        return String(data: data, encoding: .utf8) ?? fallback
    }
}

private struct APIErrorEnvelope: Decodable {
    let message: String?
}

private struct AnyEncodable: Encodable {
    private let encodeValue: (Encoder) throws -> Void

    init(_ wrapped: Encodable) {
        self.encodeValue = wrapped.encode
    }

    func encode(to encoder: Encoder) throws {
        try encodeValue(encoder)
    }
}

extension JSONDecoder {
    static var zigma: JSONDecoder {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return decoder
    }
}

extension JSONEncoder {
    static var zigma: JSONEncoder {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        return encoder
    }
}
