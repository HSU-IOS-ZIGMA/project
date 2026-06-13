import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var appState: AppState
    @State private var promises: [PromiseItem] = []
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HomeHeader {
                    Task { await loadPromises() }
                }

                if isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if let errorMessage {
                    Spacer()
                    ContentUnavailableView("약속을 불러오지 못했어요", systemImage: "exclamationmark.circle", description: Text(errorMessage))
                    Spacer()
                } else if promises.isEmpty {
                    Spacer()
                    EmptyPromisesView()
                    Spacer()
                } else {
                    List {
                        Section("진행 중인 약속 \(activePromises.count)") {
                            ForEach(activePromises) { promise in
                                NavigationLink {
                                    VoteResultView(promise: promise)
                                } label: {
                                    PromiseRow(promise: promise)
                                }
                            }
                        }

                        if !pastPromises.isEmpty {
                            Section("지난 약속 \(pastPromises.count)") {
                                ForEach(pastPromises) { promise in
                                    NavigationLink {
                                        VoteResultView(promise: promise)
                                    } label: {
                                        PromiseRow(promise: promise)
                                    }
                                }
                            }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar(.hidden, for: .navigationBar)
        }
        .onAppear {
            Task { await loadPromises() }
        }
        .onReceive(NotificationCenter.default.publisher(for: .promisesDidChange)) { _ in
            Task { await loadPromises() }
        }
    }

    private func loadPromises() async {
        if appState.accessToken == "test-account-token" {
            promises = [
                PromiseItem(
                    id: 1,
                    title: "테스트 약속",
                    promiseStatus: "진행 중",
                    promisedAt: "2026-06-09T18:00:00",
                    dayOfWeek: "화",
                    memberCount: 3,
                    isLeader: true
                )
            ]
            errorMessage = nil
            isLoading = false
            return
        }

        isLoading = true
        errorMessage = nil
        defer { isLoading = false }

        do {
            let service = PromiseService(client: APIClient(accessToken: appState.accessToken))
            promises = try await service.fetchPromises().promises
        } catch APIError.unauthorized(let message) {
            errorMessage = message
        } catch is DecodingError {
            promises = []
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private var activePromises: [PromiseItem] {
        promises.filter { !$0.isConfirmed }
    }

    private var pastPromises: [PromiseItem] {
        promises.filter(\.isConfirmed)
    }
}

private struct PromiseRow: View {
    let promise: PromiseItem

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(promise.title)
                    .font(.headline)
                    .foregroundStyle(.primary)
                Spacer()
                Text(promise.promiseStatus)
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.zigmaBlue.opacity(0.12))
                    .foregroundStyle(Color.zigmaBlue)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }

            HStack(spacing: 8) {
                Text(formattedDate)
                Text("\(promise.memberCount)명")
            }
            .font(.subheadline)
            .foregroundStyle(.secondary)
        }
        .padding(.vertical, 8)
    }

    private var formattedDate: String {
        promise.promisedAt.replacingOccurrences(of: "T", with: " ")
    }
}

private struct EmptyPromisesView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "calendar.badge.plus")
                .font(.system(size: 44, weight: .regular))
                .foregroundStyle(Color.zigmaBlue.opacity(0.75))

            Text("아직 약속이 없어요")
                .font(.system(size: 20, weight: .bold))
                .foregroundStyle(.primary)

            Text("친구들과 약속을 정해볼까요?")
                .font(.system(size: 15, weight: .medium))
                .foregroundStyle(.secondary)
        }
        .padding(.horizontal, 24)
    }
}

private struct HomeHeader: View {
    let onRefresh: () -> Void

    var body: some View {
        HStack(alignment: .center) {
            Text("홈")
                .font(.system(size: 28, weight: .bold))
                .foregroundStyle(.primary)

            Spacer()

            Button {
                onRefresh()
            } label: {
                Image(systemName: "arrow.clockwise")
                    .font(.system(size: 17, weight: .semibold))
                    .frame(width: 36, height: 36)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .foregroundStyle(Color.zigmaBlue)
            .accessibilityLabel("새로고침")
        }
        .frame(height: 56)
        .padding(.horizontal, 20)
        .padding(.top, 4)
        .background(.background)
    }
}
