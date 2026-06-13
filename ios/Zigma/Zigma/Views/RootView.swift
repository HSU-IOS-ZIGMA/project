import SwiftUI

struct RootView: View {
    @EnvironmentObject private var appState: AppState

    var body: some View {
        Group {
            if appState.isAuthenticated {
                MainTabView()
            } else {
                LoginView()
            }
        }
        .onOpenURL { url in
            appState.handleIncomingURL(url)
        }
        .task {
            await appState.joinPendingInviteIfNeeded()
        }
        .alert("알림", isPresented: Binding(
            get: { appState.inviteMessage != nil },
            set: { if !$0 { appState.inviteMessage = nil } }
        )) {
            Button("확인", role: .cancel) {
                appState.inviteMessage = nil
            }
        } message: {
            Text(appState.inviteMessage ?? "")
        }
    }
}
