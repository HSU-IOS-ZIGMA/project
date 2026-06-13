import Foundation

enum AppConfig {
    static let apiBaseURL = URL(string: "http://localhost:8080")!
    static let webSocketURL = URL(string: "ws://localhost:8080/ws")!
    static let kakaoOAuthURL = URL(string: "http://localhost:8080/oauth2/authorization/kakao?target=ios")!
}
