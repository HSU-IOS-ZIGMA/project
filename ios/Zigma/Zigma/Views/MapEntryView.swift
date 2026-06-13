import MapKit
import SwiftUI

struct MapEntryView: View {
    @EnvironmentObject private var appState: AppState
    @Binding var selectedTab: Int
    @State private var promises: [PromiseItem] = []
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        NavigationStack {
            ZStack {
                Map(position: .constant(.region(defaultRegion)))
                    .ignoresSafeArea(edges: .top)
                    .allowsHitTesting(false)

                VStack {
                    Spacer()

                    if isLoading {
                        MapEntryPanel {
                            ProgressView()
                        }
                    } else if let singlePromise = activePromises.first, activePromises.count == 1 {
                        PromiseMapView(promiseId: singlePromise.id, promiseTitle: singlePromise.title)
                    } else if activePromises.isEmpty {
                        MapEntryPanel {
                            VStack(spacing: 14) {
                                Image(systemName: "exclamationmark.circle")
                                    .font(.system(size: 42))
                                    .foregroundStyle(Color.zigmaBlue)
                                Text("아직 진행 중인 약속이 없어요")
                                    .font(.system(size: 22, weight: .semibold))
                                Text("약속을 만들어 주세요")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Button("새 약속 만들기") {
                                    selectedTab = 2
                                }
                                .buttonStyle(ZigmaButtonStyle())
                            }
                        }
                    } else {
                        MapPromiseSelectionPanel(promises: activePromises)
                    }
                }
            }
            .task {
                await loadPromises()
            }
            .onChange(of: selectedTab) { _, tab in
                if tab == 1 {
                    Task { await loadPromises() }
                }
            }
        }
    }

    private var activePromises: [PromiseItem] {
        promises.filter { !$0.isConfirmed }
    }

    private var defaultRegion: MKCoordinateRegion {
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 37.5823688, longitude: 127.0111299),
            span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04)
        )
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
            return
        }

        isLoading = true
        defer { isLoading = false }

        do {
            let service = PromiseService(client: APIClient(accessToken: appState.accessToken))
            promises = try await service.fetchPromises().promises
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
            promises = []
        }
    }
}

private struct MapPromiseSelectionPanel: View {
    let promises: [PromiseItem]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            VStack(alignment: .leading, spacing: 4) {
                Text("여러 약속이 진행 중이에요")
                    .font(.system(size: 22, weight: .semibold))
                Text("후보지를 추가할 약속을 선택해 주세요")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            ForEach(promises) { promise in
                NavigationLink {
                    PromiseMapView(promiseId: promise.id, promiseTitle: promise.title)
                } label: {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(promise.title)
                                .font(.headline)
                                .foregroundStyle(.primary)
                            Text("\(promise.promisedAt.replacingOccurrences(of: "T", with: " ")) · \(promise.memberCount)명")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(.secondary)
                    }
                    .padding(14)
                    .background(Color.gray.opacity(0.08))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.background)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 12, y: -4)
        .padding()
    }
}

private struct MapEntryPanel<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack {
            content
        }
        .multilineTextAlignment(.center)
        .padding(24)
        .frame(maxWidth: .infinity)
        .background(.background)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 12, y: -4)
        .padding()
    }
}
