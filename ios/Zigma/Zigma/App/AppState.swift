import Foundation

@MainActor
final class AppState: ObservableObject {
    @Published var inviteMessage: String?
    @Published var accessToken: String? {
        didSet {
            if let accessToken {
                UserDefaults.standard.set(accessToken, forKey: "accessToken")
            } else {
                UserDefaults.standard.removeObject(forKey: "accessToken")
            }
        }
    }

    init() {
        let savedToken = UserDefaults.standard.string(forKey: "accessToken")
        if savedToken == "local-demo-token" {
            UserDefaults.standard.removeObject(forKey: "accessToken")
            self.accessToken = nil
        } else {
            self.accessToken = savedToken
        }
    }

    var isAuthenticated: Bool {
        accessToken?.isEmpty == false
    }

    var tokenPreview: String {
        guard let accessToken, !accessToken.isEmpty else {
            return "none"
        }

        if accessToken.count <= 18 {
            return accessToken
        }

        return String(accessToken.prefix(18)) + "..."
    }

    func loginForLocalDemo(token: String = "local-demo-token") {
        accessToken = token
        Task { await joinPendingInviteIfNeeded() }
    }

    func loginAsTestAccount() {
        accessToken = "test-account-token"
        Task { await joinPendingInviteIfNeeded() }
    }

    func logout() {
        accessToken = nil
    }

    func handleIncomingURL(_ url: URL) {
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
              let inviteCode = components.queryItems?.first(where: { $0.name == "inviteCode" })?.value,
              !inviteCode.isEmpty else {
            return
        }

        UserDefaults.standard.set(inviteCode, forKey: "pendingInviteCode")
        Task { await joinPendingInviteIfNeeded() }
    }

    func joinPendingInviteIfNeeded() async {
        guard let inviteCode = UserDefaults.standard.string(forKey: "pendingInviteCode") else {
            return
        }

        guard let accessToken, !accessToken.isEmpty, accessToken != "test-account-token" else {
            inviteMessage = "로그인 후 초대 약속에 참여할 수 있어요."
            return
        }

        do {
            try await PromiseService(client: APIClient(accessToken: accessToken)).joinPromise(inviteCode: inviteCode)
            UserDefaults.standard.removeObject(forKey: "pendingInviteCode")
            inviteMessage = "초대 약속에 참여했어요."
            NotificationCenter.default.post(name: .promisesDidChange, object: nil)
        } catch APIError.conflict(_) {
            UserDefaults.standard.removeObject(forKey: "pendingInviteCode")
            inviteMessage = "이미 참여 중인 약속이에요."
            NotificationCenter.default.post(name: .promisesDidChange, object: nil)
        } catch {
            inviteMessage = error.localizedDescription
        }
    }
}
