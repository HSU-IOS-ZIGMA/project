import SwiftUI

struct AccountView: View {
    @EnvironmentObject private var appState: AppState

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    HStack(spacing: 14) {
                        Circle()
                            .fill(Color.zigmaBlue)
                            .frame(width: 52, height: 52)
                            .overlay(Text("Z").font(.title2.bold()).foregroundStyle(.white))

                        VStack(alignment: .leading, spacing: 4) {
                            Text("로컬 데모 사용자")
                                .font(.headline)
                            Text("localhost:8080 연결")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .padding(.vertical, 8)
                }

                Section {
                    Button(role: .destructive) {
                        appState.logout()
                    } label: {
                        Text("로그아웃")
                    }
                }
            }
            .navigationTitle("계정")
        }
    }
}

