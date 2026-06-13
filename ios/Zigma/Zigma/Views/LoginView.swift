import AuthenticationServices
import SwiftUI
import UIKit

struct LoginView: View {
    @EnvironmentObject private var appState: AppState
    @State private var authSession: ASWebAuthenticationSession?
    @State private var errorMessage: String?
    @State private var isLoggingIn = false

    var body: some View {
        VStack(spacing: 40) {
            VStack(spacing: 12) {
                ZStack {
                    Image("mainLogoIcon")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 68, height: 68)
                }

                Text("ZIGMA")
                    .font(.system(size: 20, weight: .heavy))
                .foregroundStyle(Color.zigmaBlue)
            }

            VStack(spacing: 12) {
                Button {
                    startKakaoLogin()
                } label: {
                    Text("카카오로 시작하기")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(ZigmaButtonStyle(background: Color(red: 1.0, green: 0.9, blue: 0.0), foreground: .black))

                HStack(spacing: 10) {
                    Button {
                        Task { await loginWithTestAccount(slot: 1) }
                    } label: {
                        Text("테스트 계정 1")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(ZigmaButtonStyle(background: Color.zigmaBlue, foreground: .white))

                    Button {
                        Task { await loginWithTestAccount(slot: 2) }
                    } label: {
                        Text("테스트 계정 2")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(ZigmaButtonStyle(background: Color.zigmaBlue.opacity(0.85), foreground: .white))
                }
                .disabled(isLoggingIn)

                if let errorMessage {
                    Text(errorMessage)
                        .font(.caption)
                        .foregroundStyle(.red)
                        .multilineTextAlignment(.center)
                }
            }
            .padding(.horizontal, 24)
        }
    }

    private func startKakaoLogin() {
        errorMessage = nil
        appState.logout()

        let session = ASWebAuthenticationSession(
            url: AppConfig.kakaoOAuthURL,
            callbackURLScheme: "zigma"
        ) { callbackURL, error in
            Task { @MainActor in
                if let error {
                    errorMessage = error.localizedDescription
                    return
                }

                guard
                    let callbackURL,
                    let components = URLComponents(url: callbackURL, resolvingAgainstBaseURL: false),
                    let accessToken = components.queryItems?.first(where: { $0.name == "accessToken" })?.value,
                    !accessToken.isEmpty
                else {
                    errorMessage = "카카오 로그인 토큰을 읽지 못했어요."
                    return
                }

                appState.loginForLocalDemo(token: accessToken)
            }
        }

        session.presentationContextProvider = AuthenticationPresentationContextProvider.shared
        session.prefersEphemeralWebBrowserSession = true
        authSession = session
        session.start()
    }

    private func loginWithTestAccount(slot: Int) async {
        errorMessage = nil
        isLoggingIn = true
        defer { isLoggingIn = false }

        do {
            let response: APIEnvelope<DevLoginData> = try await APIClient().request(
                "dev/auth/login",
                method: "POST",
                queryItems: [URLQueryItem(name: "slot", value: "\(slot)")]
            )
            appState.loginForLocalDemo(token: response.data.accessToken)
        } catch {
            errorMessage = "테스트 계정 로그인 실패: \(error.localizedDescription)"
        }
    }
}

private struct DevLoginData: Decodable {
    let accessToken: String
    let userId: Int
    let nickname: String
}

final class AuthenticationPresentationContextProvider: NSObject, ASWebAuthenticationPresentationContextProviding {
    static let shared = AuthenticationPresentationContextProvider()

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first { $0.isKeyWindow } ?? ASPresentationAnchor()
    }
}
