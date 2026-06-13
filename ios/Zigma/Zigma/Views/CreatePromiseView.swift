import SwiftUI

struct CreatePromiseView: View {
    @EnvironmentObject private var appState: AppState
    @Binding var selectedTab: Int
    @State private var title = ""
    @State private var date = Date()
    @State private var category = "식사"
    @State private var isMultipleVoting = true
    @State private var isSubmitting = false
    @State private var message: String?

    private let categories = ["식사", "카페", "영화", "액티비티", "스터디", "파티"]

    var body: some View {
        NavigationStack {
            Form {
                Section("약속명") {
                    TextField("약속명을 입력해 주세요", text: $title)
                }

                Section("날짜 · 시간") {
                    DatePicker("약속 시간", selection: $date)
                }

                Section("약속 주제") {
                    Picker("주제", selection: $category) {
                        ForEach(categories, id: \.self) { category in
                            Text(category)
                        }
                    }
                }

                Section {
                    Toggle("장소 복수 투표", isOn: $isMultipleVoting)
                }

                if let message {
                    Section {
                        Text(message)
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle("새 약속")
            .safeAreaInset(edge: .bottom) {
                Button {
                    Task { await submit() }
                } label: {
                    if isSubmitting {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                    } else {
                        Text("약속 만들기")
                            .frame(maxWidth: .infinity)
                    }
                }
                .disabled(title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isSubmitting)
                .buttonStyle(ZigmaButtonStyle())
                .padding()
                .background(.background)
            }
        }
    }

    private func submit() async {
        isSubmitting = true
        message = nil
        defer { isSubmitting = false }

        do {
            if appState.accessToken == "test-account-token" {
                message = "약속을 만들었어요."
                title = ""
                selectedTab = 0
                return
            }

            let formatter = ISO8601DateFormatter()
            formatter.formatOptions = [.withInternetDateTime]
            let request = CreatePromiseRequest(
                title: title,
                promisedAt: formatter.string(from: date),
                category: category,
                endAt: nil,
                isMultipleVoting: isMultipleVoting
            )
            let service = PromiseService(client: APIClient(accessToken: appState.accessToken))
            _ = try await service.createPromise(request)
            message = "약속을 만들었어요."
            title = ""
            selectedTab = 0
        } catch {
            message = error.localizedDescription
        }
    }
}
