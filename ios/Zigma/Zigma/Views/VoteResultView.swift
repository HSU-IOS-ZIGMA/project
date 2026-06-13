import SwiftUI
import UIKit

struct VoteResultView: View {
    @EnvironmentObject private var appState: AppState
    @State private var candidates: [CandidatePlace] = []
    @State private var promiseDetail: PromiseDetail?
    @State private var selectedIds: Set<Int> = []
    @State private var hasVoted = false
    @State private var isLoading = false
    @State private var isSubmitting = false
    @State private var errorMessage: String?
    @State private var alertMessage: String?
    @State private var confirmCandidate: CandidatePlace?
    @State private var isRevote = false

    let promise: PromiseItem

    var body: some View {
        VStack(spacing: 0) {
            header

            if isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if sortedCandidates.isEmpty {
                Spacer()
                ContentUnavailableView("투표 중인 장소가 없어요", systemImage: "mappin.slash", description: Text("지도 탭에서 후보 장소를 추가해 주세요."))
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 14) {
                        if isTie && !isRevote {
                            HStack(spacing: 6) {
                                Image(systemName: "exclamationmark.triangle")
                                Text("동점 시 재투표를 진행해 주세요")
                            }
                            .font(.caption)
                            .foregroundStyle(.orange)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 16)
                        }

                        ForEach(displayCandidates) { candidate in
                            CandidateVoteCard(
                                candidate: candidate,
                                memberCount: promiseDetail?.memberCount ?? promise.memberCount,
                                status: status(for: candidate),
                                isSelected: selectedIds.contains(candidate.id),
                                onTap: { toggleSelection(candidate) }
                            )
                            .padding(.horizontal, 16)
                        }
                    }
                    .padding(.vertical, 16)
                    .padding(.bottom, 96)
                }
            }
        }
        .navigationTitle(isLeader ? "장소 결정" : "장소 투표")
        .navigationBarTitleDisplayMode(.inline)
        .safeAreaInset(edge: .bottom) {
            bottomAction
        }
        .alert("이 장소를 확정할까요?", isPresented: Binding(
            get: { confirmCandidate != nil },
            set: { if !$0 { confirmCandidate = nil } }
        )) {
            Button("취소", role: .cancel) {
                confirmCandidate = nil
            }
            Button("확정하기") {
                if let confirmCandidate {
                    Task { await confirm(candidate: confirmCandidate) }
                }
            }
        } message: {
            Text(confirmCandidate?.name ?? "")
        }
        .alert("알림", isPresented: Binding(
            get: { alertMessage != nil },
            set: { if !$0 { alertMessage = nil } }
        )) {
            Button("확인", role: .cancel) {
                alertMessage = nil
            }
        } message: {
            Text(alertMessage ?? "")
        }
        .task {
            await load()
        }
        .toolbar {
            if isLeader && !isPromiseConfirmed {
                Button {
                    Task { await copyInviteLink() }
                } label: {
                    Label("초대", systemImage: "person.badge.plus")
                }
                .disabled(isSubmitting)
            }
        }
    }

    private var header: some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading, spacing: 6) {
                Text(promise.title)
                    .font(.system(size: 24, weight: .semibold))
                Text("\(promiseDetail?.memberCount ?? promise.memberCount)명 참여")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Text(promiseDetail?.promiseStatus ?? promise.promiseStatus)
                .font(.caption.weight(.semibold))
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(Color.zigmaBlue.opacity(0.12))
                .foregroundStyle(Color.zigmaBlue)
                .clipShape(Capsule())
        }
        .padding(16)
    }

    private var bottomAction: some View {
        VStack {
            if let errorMessage {
                Text(errorMessage)
                    .font(.caption)
                    .foregroundStyle(.red)
                    .lineLimit(2)
            }

            Button {
                if isLeader {
                    if isTie && !isRevote {
                        Task { await startRevote() }
                    } else {
                        confirmCandidate = confirmTarget
                    }
                } else if hasVoted {
                    Task { await cancelVote() }
                } else {
                    Task { await submitVote() }
                }
            } label: {
                if isSubmitting {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text(buttonText)
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(ZigmaButtonStyle())
            .disabled(buttonDisabled || isSubmitting)
        }
        .padding()
        .background(.background)
    }

    private var isLeader: Bool {
        promiseDetail?.isLeader ?? promise.isLeader
    }

    private var isMultipleVoting: Bool {
        promiseDetail?.isMultipleVoting ?? true
    }

    private var isPromiseConfirmed: Bool {
        let status = promiseDetail?.promiseStatus ?? promise.promiseStatus
        return status == "확정 완료" || status == "CONFIRMED"
    }

    private var sortedCandidates: [CandidatePlace] {
        candidates.sorted { lhs, rhs in
            lhs.voteInfo.voteCount > rhs.voteInfo.voteCount
        }
    }

    private var displayCandidates: [CandidatePlace] {
        let source = isRevote ? topCandidates : candidates
        return source.sorted { lhs, rhs in
            lhs.voteInfo.voteCount > rhs.voteInfo.voteCount
        }
    }

    private var hasAnyVote: Bool {
        candidates.contains { $0.voteInfo.voteCount > 0 }
    }

    private var maxVote: Int {
        candidates.map(\.voteInfo.voteCount).max() ?? 0
    }

    private var topCandidates: [CandidatePlace] {
        candidates.filter { $0.voteInfo.voteCount == maxVote }
    }

    private var isTie: Bool {
        hasAnyVote && topCandidates.count > 1
    }

    private var confirmTarget: CandidatePlace? {
        if let selected = selectedIds.first {
            return displayCandidates.first { $0.id == selected }
        }
        return isRevote ? nil : topCandidates.first
    }

    private var buttonText: String {
        if isLeader {
            if isTie && !isRevote {
                return "다시 투표하기"
            }
            return "장소 결정하기"
        }
        return hasVoted ? "다시 투표하기" : "투표하기"
    }

    private var buttonDisabled: Bool {
        if isLeader {
            if isTie && !isRevote {
                return false
            }
            if isRevote {
                return selectedIds.isEmpty
            }
            return !hasAnyVote && selectedIds.isEmpty
        }
        return !hasVoted && selectedIds.isEmpty
    }

    private func status(for candidate: CandidatePlace) -> VoteCardStatus {
        guard hasAnyVote, candidate.voteInfo.voteCount == maxVote else { return .normal }
        return isTie ? .tie : .best
    }

    private func toggleSelection(_ candidate: CandidatePlace) {
        if isLeader {
            guard isRevote || !isTie else { return }
            selectedIds = [candidate.id]
            return
        }

        if isMultipleVoting {
            if selectedIds.contains(candidate.id) {
                selectedIds.remove(candidate.id)
            } else {
                selectedIds.insert(candidate.id)
            }
        } else {
            selectedIds = selectedIds.contains(candidate.id) ? [] : [candidate.id]
        }
    }

    private func load() async {
        if appState.accessToken == "test-account-token" {
            let sample = CandidatePlace.mock(id: 1, name: "테스트 장소", latitude: 37.5823688, longitude: 127.0111299).withVote(isMyVote: false)
            candidates = [sample]
            selectedIds = []
            hasVoted = false
            return
        }

        isLoading = true
        defer { isLoading = false }

        do {
            let client = APIClient(accessToken: appState.accessToken)
            async let detail = PromiseService(client: client).fetchPromiseDetail(id: promise.id)
            async let places = CandidatePlaceService(client: client).fetchCandidatePlaces(promiseId: promise.id)
            promiseDetail = try await detail
            candidates = try await places.candidates
            selectedIds = Set(candidates.filter { $0.voteInfo.isMyVote }.map(\.id))
            hasVoted = !selectedIds.isEmpty
            if isRevote {
                selectedIds = []
                hasVoted = false
            }
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func submitVote() async {
        isSubmitting = true
        defer { isSubmitting = false }

        if appState.accessToken == "test-account-token" {
            candidates = candidates.map { selectedIds.contains($0.id) ? $0.withVote(isMyVote: true) : $0 }
            hasVoted = true
            return
        }

        do {
            let service = CandidatePlaceService(client: APIClient(accessToken: appState.accessToken))
            for id in selectedIds {
                try await service.vote(promiseId: promise.id, candidateId: id)
            }
            await load()
        } catch APIError.conflict(let message) {
            alertMessage = message
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func cancelVote() async {
        isSubmitting = true
        defer { isSubmitting = false }

        if appState.accessToken == "test-account-token" {
            candidates = candidates.map { $0.withVote(isMyVote: false) }
            selectedIds = []
            hasVoted = false
            return
        }

        do {
            let service = CandidatePlaceService(client: APIClient(accessToken: appState.accessToken))
            for id in selectedIds {
                try await service.deleteVote(promiseId: promise.id, candidateId: id)
            }
            await load()
        } catch APIError.conflict(let message) {
            alertMessage = message
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func confirm(candidate: CandidatePlace) async {
        isSubmitting = true
        defer {
            isSubmitting = false
            confirmCandidate = nil
        }

        if appState.accessToken == "test-account-token" {
            candidates = candidates.map { $0.id == candidate.id ? $0.withConfirmed(true) : $0.withConfirmed(false) }
            return
        }

        do {
            let service = CandidatePlaceService(client: APIClient(accessToken: appState.accessToken))
            try await service.confirmPlace(promiseId: promise.id, candidateId: candidate.id)
            await load()
            NotificationCenter.default.post(name: .promisesDidChange, object: nil)
        } catch APIError.conflict(let message) {
            alertMessage = message
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func copyInviteLink() async {
        guard appState.accessToken != "test-account-token" else {
            alertMessage = "테스트 계정에서는 초대 링크를 만들 수 없어요."
            return
        }

        isSubmitting = true
        defer { isSubmitting = false }

        do {
            let service = PromiseService(client: APIClient(accessToken: appState.accessToken))
            let invite = try await service.createInviteCode(promiseId: promise.id)
            var components = URLComponents()
            components.scheme = "zigma"
            components.host = "map"
            components.path = "/\(invite.promiseId)"
            components.queryItems = [
                URLQueryItem(name: "inviteCode", value: invite.inviteCode)
            ]

            guard let inviteURL = components.url else {
                alertMessage = "초대 링크를 만들지 못했어요."
                return
            }

            UIPasteboard.general.string = inviteURL.absoluteString
            alertMessage = "초대 링크가 복사되었습니다."
        } catch {
            alertMessage = "초대 링크 복사에 실패했습니다."
        }
    }

    private func startRevote() async {
        isSubmitting = true
        defer { isSubmitting = false }

        if appState.accessToken == "test-account-token" {
            candidates = candidates.map { $0.withVoteCount(0, isMyVote: false) }
            selectedIds = []
            hasVoted = false
            isRevote = true
            return
        }

        do {
            let service = CandidatePlaceService(client: APIClient(accessToken: appState.accessToken))
            try await service.revote(promiseId: promise.id)
            isRevote = true
            selectedIds = []
            hasVoted = false
            await load()
        } catch APIError.conflict(let message) {
            alertMessage = message
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

private enum VoteCardStatus {
    case normal
    case best
    case tie
}

private struct CandidateVoteCard: View {
    let candidate: CandidatePlace
    let memberCount: Int
    let status: VoteCardStatus
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 14) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 5) {
                        Text(candidate.name)
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundStyle(.primary)
                        Text("📍 \(Int(candidate.distance))m \(candidate.address)")
                            .font(.caption)
                            .foregroundStyle(.primary)
                            .lineLimit(1)
                    }

                    Spacer()

                    badge
                }

                VStack(alignment: .trailing, spacing: 10) {
                    Text("\(candidate.voteInfo.voteCount) / \(memberCount) 표")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(status == .tie ? .orange : Color.zigmaBlue)

                    GeometryReader { proxy in
                        ZStack(alignment: .leading) {
                            Capsule()
                                .fill(Color.gray.opacity(0.25))
                            Capsule()
                                .fill(status == .tie ? .orange : Color.zigmaBlue)
                                .frame(width: progressWidth(totalWidth: proxy.size.width))
                        }
                    }
                    .frame(height: 6)
                }

                Text("\(candidate.voteInfo.creator.nickname) \(voteMembersText)")
                    .font(.subheadline)
                    .foregroundStyle(.primary)
            }
            .padding(18)
            .background(background)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(borderColor, lineWidth: isSelected ? 1.5 : 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private var badge: some View {
        switch status {
        case .best:
            Text("1위")
                .font(.caption.weight(.semibold))
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(Color.green.opacity(0.12))
                .foregroundStyle(.green)
                .clipShape(Capsule())
        case .tie:
            Text("동점")
                .font(.caption.weight(.semibold))
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(.orange)
                .foregroundStyle(.white)
                .clipShape(Capsule())
        case .normal:
            EmptyView()
        }
    }

    private var background: Color {
        if status == .best || isSelected {
            return Color.zigmaBlue.opacity(0.06)
        }
        if status == .tie {
            return Color.orange.opacity(0.10)
        }
        return .clear
    }

    private var borderColor: Color {
        if isSelected {
            return status == .tie ? .orange.opacity(0.5) : Color.zigmaBlue
        }
        return Color.gray.opacity(0.35)
    }

    private var voteMembersText: String {
        let voters = candidate.voteInfo.voters
            .filter { $0.userId != candidate.voteInfo.creator.userId }
            .map(\.nickname)
            .joined(separator: ", ")
        return voters
    }

    private func progressWidth(totalWidth: CGFloat) -> CGFloat {
        guard memberCount > 0 else { return 0 }
        return totalWidth * CGFloat(candidate.voteInfo.voteCount) / CGFloat(memberCount)
    }
}
