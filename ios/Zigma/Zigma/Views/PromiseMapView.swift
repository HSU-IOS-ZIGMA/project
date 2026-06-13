import MapKit
import SwiftUI
import UIKit

struct PromiseMapView: View {
    @EnvironmentObject private var appState: AppState
    @StateObject private var presence = PresenceViewModel()
    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 37.5823688, longitude: 127.0111299),
            span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04)
        )
    )
    @State private var candidates: [CandidatePlace] = []
    @State private var errorMessage: String?
    @State private var alertMessage: String?
    @State private var selectedCoordinate: CLLocationCoordinate2D?
    @State private var selectedCandidate: CandidatePlace?
    @State private var comments: [CommentItem] = []
    @State private var isCommentMode = false
    @State private var commentCoordinate: CLLocationCoordinate2D?
    @State private var selectedComment: CommentItem?
    @State private var cameraRegion = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 37.5823688, longitude: 127.0111299),
        span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04)
    )
    @State private var isSubmitting = false

    let promiseId: Int
    var promiseTitle: String = "약속 지도"

    var body: some View {
        ZStack(alignment: .topLeading) {
            NativePromiseMapView(
                region: $cameraRegion,
                candidates: candidates,
                comments: comments,
                selectedCoordinate: selectedCoordinate,
                isCommentMode: isCommentMode,
                onMapTap: { coordinate in
                    if isCommentMode {
                        commentCoordinate = coordinate
                        isCommentMode = false
                        selectedCoordinate = nil
                        selectedCandidate = nil
                        selectedComment = nil
                    } else {
                        selectedCoordinate = coordinate
                        selectedCandidate = nil
                        selectedComment = nil
                    }
                },
                onCandidateTap: { candidate in
                    selectedCandidate = candidate
                    selectedCoordinate = nil
                    selectedComment = nil
                    commentCoordinate = nil
                },
                onCommentTap: { comment in
                    selectedComment = comment
                    selectedCoordinate = nil
                    selectedCandidate = nil
                    commentCoordinate = nil
                },
                onRegionChanged: { region in
                    cameraRegion = region
                    Task { await loadComments() }
                }
            )
            .ignoresSafeArea(edges: .top)

            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text(promiseTitle)
                        .font(.headline)
                    Text(presence.modeLabel)
                        .font(.caption2.weight(.semibold))
                        .padding(.horizontal, 6)
                        .padding(.vertical, 3)
                        .background(Color.zigmaBlue.opacity(0.12))
                        .foregroundStyle(Color.zigmaBlue)
                        .clipShape(Capsule())
                }

                if presence.members.isEmpty {
                    Text("접속자 없음")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                } else {
                    HStack(spacing: -6) {
                        ForEach(presence.members.prefix(5)) { member in
                            Circle()
                                .fill(Color.zigmaBlue)
                                .frame(width: 28, height: 28)
                                .overlay(Text(String(member.nickName.prefix(1))).font(.caption).foregroundStyle(.white))
                                .overlay(Circle().stroke(.white, lineWidth: 2))
                        }
                    }
                }

                if let errorMessage {
                    Text(errorMessage)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
            .padding(14)
            .background(.regularMaterial)
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .padding()

            VStack {
                Spacer()
                if let commentCoordinate {
                    CommentInputPanel(
                        coordinate: commentCoordinate,
                        isSubmitting: isSubmitting,
                        onClose: { self.commentCoordinate = nil },
                        onSubmit: { content in
                            Task { await addComment(content: content, coordinate: commentCoordinate) }
                        }
                    )
                } else if let selectedComment {
                    CommentPreviewPanel(comment: selectedComment) {
                        self.selectedComment = nil
                    }
                } else if let selectedCoordinate {
                    SelectedLocationPanel(
                        coordinate: selectedCoordinate,
                        isSubmitting: isSubmitting,
                        onAdd: { name in
                            Task { await addCandidate(name: name, coordinate: selectedCoordinate) }
                        }
                    )
                } else if let selectedCandidate {
                    CandidateActionPanel(
                        candidate: selectedCandidate,
                        isSubmitting: isSubmitting,
                        onVote: { Task { await vote(candidate: selectedCandidate) } },
                        onDeleteVote: { Task { await deleteVote(candidate: selectedCandidate) } },
                        onDeleteCandidate: { Task { await deleteCandidate(candidate: selectedCandidate) } }
                    )
                } else {
                    CandidateBottomPanel(candidates: candidates)
                }
            }

            VStack {
                Spacer()
                HStack {
                    Spacer()
                    Button {
                        isCommentMode.toggle()
                        selectedCoordinate = nil
                        selectedCandidate = nil
                        selectedComment = nil
                        commentCoordinate = nil
                    } label: {
                        Image(systemName: isCommentMode ? "xmark" : "bubble.left.fill")
                            .font(.system(size: 22, weight: .semibold))
                            .foregroundStyle(.white)
                            .frame(width: 58, height: 58)
                            .background(isCommentMode ? Color.gray : Color.zigmaBlue)
                            .clipShape(Circle())
                            .shadow(color: .black.opacity(0.16), radius: 8, y: 4)
                    }
                    .padding(.trailing, 24)
                    .padding(.bottom, selectedCoordinate == nil && selectedCandidate == nil && commentCoordinate == nil && selectedComment == nil ? 150 : 240)
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            Button("새로고침") {
                Task {
                    await loadCandidates()
                    await loadComments()
                }
            }
            Button("재연결") {
                if let token = appState.accessToken, token != "test-account-token" {
                    presence.startLive(promiseId: promiseId, accessToken: token, forceReconnect: true)
                }
            }
        }
        .task {
            if let token = appState.accessToken, token != "test-account-token" {
                presence.startLive(promiseId: promiseId, accessToken: token)
            } else {
                presence.startOffline()
            }
            await loadCandidates()
            await loadComments()
        }
        .onDisappear {
            presence.stop()
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
    }

    private func loadCandidates() async {
        if appState.accessToken == "test-account-token" {
            candidates = []
            errorMessage = nil
            return
        }

        do {
            let service = CandidatePlaceService(client: APIClient(accessToken: appState.accessToken))
            candidates = try await service.fetchCandidatePlaces(promiseId: promiseId).candidates
            errorMessage = nil
        } catch {
            errorMessage = "로컬 서버 연결 실패: \(error.localizedDescription)"
        }
    }

    private func loadComments() async {
        if appState.accessToken == "test-account-token" {
            return
        }

        do {
            let service = CommentService(client: APIClient(accessToken: appState.accessToken))
            comments = try await service.fetchComments(promiseId: promiseId, region: cameraRegion).comments
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func addCandidate(name: String, coordinate: CLLocationCoordinate2D) async {
        if appState.accessToken == "test-account-token" {
            let candidate = CandidatePlace.mock(
                id: (candidates.map(\.id).max() ?? 0) + 1,
                name: name,
                latitude: coordinate.latitude,
                longitude: coordinate.longitude
            )
            candidates.append(candidate)
            selectedCandidate = candidate
            selectedCoordinate = nil
            return
        }

        isSubmitting = true
        defer { isSubmitting = false }

        do {
            let request = AddCandidatePlaceRequest(
                name: name,
                address: "위도 \(String(format: "%.5f", coordinate.latitude)), 경도 \(String(format: "%.5f", coordinate.longitude))",
                latitude: coordinate.latitude,
                longitude: coordinate.longitude,
                category: ""
            )
            let service = CandidatePlaceService(client: APIClient(accessToken: appState.accessToken))
            _ = try await service.addCandidatePlace(promiseId: promiseId, request: request)
            selectedCoordinate = nil
            await loadCandidates()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func addComment(content: String, coordinate: CLLocationCoordinate2D) async {
        let trimmedContent = content.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedContent.isEmpty else { return }

        if appState.accessToken == "test-account-token" {
            comments.append(
                CommentItem(
                    id: (comments.map(\.id).max() ?? 0) + 1,
                    userId: 1,
                    nickname: "나",
                    profileImageUrl: nil,
                    content: trimmedContent,
                    latitude: coordinate.latitude,
                    longitude: coordinate.longitude,
                    createdAt: ""
                )
            )
            commentCoordinate = nil
            return
        }

        isSubmitting = true
        defer { isSubmitting = false }

        do {
            let request = CreateCommentRequest(
                content: trimmedContent,
                latitude: coordinate.latitude,
                longitude: coordinate.longitude
            )
            let service = CommentService(client: APIClient(accessToken: appState.accessToken))
            _ = try await service.createComment(promiseId: promiseId, request: request)
            commentCoordinate = nil
            await loadComments()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func vote(candidate: CandidatePlace) async {
        if appState.accessToken == "test-account-token" {
            upsertLocalCandidate(candidate.withVote(isMyVote: true))
            return
        }

        isSubmitting = true
        defer { isSubmitting = false }

        do {
            let service = CandidatePlaceService(client: APIClient(accessToken: appState.accessToken))
            try await service.vote(promiseId: promiseId, candidateId: candidate.id)
            await loadCandidates()
            selectedCandidate = candidates.first { $0.id == candidate.id }
        } catch APIError.conflict(let message) {
            alertMessage = message
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func deleteVote(candidate: CandidatePlace) async {
        if appState.accessToken == "test-account-token" {
            upsertLocalCandidate(candidate.withVote(isMyVote: false))
            return
        }

        isSubmitting = true
        defer { isSubmitting = false }

        do {
            let service = CandidatePlaceService(client: APIClient(accessToken: appState.accessToken))
            try await service.deleteVote(promiseId: promiseId, candidateId: candidate.id)
            let fallbackCandidate = candidate.withVote(isMyVote: false)
            await loadCandidates()
            if let refreshedCandidate = candidates.first(where: { $0.id == candidate.id }) {
                selectedCandidate = refreshedCandidate
            } else {
                upsertLocalCandidate(fallbackCandidate)
            }
        } catch APIError.conflict(let message) {
            alertMessage = message
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func deleteCandidate(candidate: CandidatePlace) async {
        if appState.accessToken == "test-account-token" {
            candidates.removeAll { $0.id == candidate.id }
            selectedCandidate = nil
            return
        }

        isSubmitting = true
        defer { isSubmitting = false }

        do {
            let service = CandidatePlaceService(client: APIClient(accessToken: appState.accessToken))
            try await service.deleteCandidatePlace(promiseId: promiseId, candidateId: candidate.id)
            selectedCandidate = nil
            await loadCandidates()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func upsertLocalCandidate(_ candidate: CandidatePlace) {
        if let index = candidates.firstIndex(where: { $0.id == candidate.id }) {
            candidates[index] = candidate
        } else {
            candidates.append(candidate)
        }
        selectedCandidate = candidate
        selectedCoordinate = nil
    }

}

private struct CandidateBottomPanel: View {
    let candidates: [CandidatePlace]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("후보 장소 \(candidates.count)")
                    .font(.headline)
                Spacer()
            }

            if candidates.isEmpty {
                Text("서버에서 후보 장소를 불러오면 여기에 표시됩니다.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            } else {
                ForEach(candidates.prefix(3)) { candidate in
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(candidate.name)
                                .font(.subheadline.weight(.semibold))
                            Text(candidate.address)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .lineLimit(1)
                        }
                        Spacer()
                        Text("\(candidate.voteInfo.voteCount)")
                            .font(.headline)
                            .foregroundStyle(Color.zigmaBlue)
                    }
                }
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.background)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 12, y: -4)
        .padding()
        .contentShape(Rectangle())
        .onTapGesture {}
    }
}

private struct CommentInputPanel: View {
    let coordinate: CLLocationCoordinate2D
    let isSubmitting: Bool
    let onClose: () -> Void
    let onSubmit: (String) -> Void
    @State private var content = ""

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("댓글 추가")
                    .font(.headline)
                Spacer()
                Button(action: onClose) {
                    Image(systemName: "xmark")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(width: 32, height: 32)
                        .background(Color.gray)
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
            }

            TextField("댓글 추가", text: $content)
                .textInputAutocapitalization(.never)
                .padding(14)
                .background(Color.gray.opacity(0.12))
                .clipShape(RoundedRectangle(cornerRadius: 10))

            HStack {
                Text("위도 \(String(format: "%.5f", coordinate.latitude)), 경도 \(String(format: "%.5f", coordinate.longitude))")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                Spacer()
                Button {
                    onSubmit(content)
                } label: {
                    if isSubmitting {
                        ProgressView()
                            .frame(width: 38, height: 38)
                    } else {
                        Image(systemName: "paperplane.fill")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundStyle(.white)
                            .frame(width: 38, height: 38)
                            .background(content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? Color.gray.opacity(0.35) : Color.zigmaBlue)
                            .clipShape(Circle())
                    }
                }
                .disabled(content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isSubmitting)
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.background)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 12, y: -4)
        .padding()
        .contentShape(Rectangle())
        .onTapGesture {}
    }
}

private struct CommentPreviewPanel: View {
    let comment: CommentItem
    let onClose: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Circle()
                    .fill(Color.black.opacity(0.78))
                    .frame(width: 34, height: 34)
                    .overlay(Text(String(comment.nickname.prefix(1))).font(.caption.weight(.bold)).foregroundStyle(.white))

                VStack(alignment: .leading, spacing: 2) {
                    Text(comment.nickname)
                        .font(.subheadline.weight(.semibold))
                    Text(comment.createdAt)
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                Button(action: onClose) {
                    Image(systemName: "xmark")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(.secondary)
                }
                .buttonStyle(.plain)
            }

            Text(comment.content)
                .font(.subheadline)
                .foregroundStyle(.primary)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.background)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 12, y: -4)
        .padding()
        .contentShape(Rectangle())
        .onTapGesture {}
    }
}

private struct SelectedLocationPanel: View {
    let coordinate: CLLocationCoordinate2D
    let isSubmitting: Bool
    let onAdd: (String) -> Void
    @State private var placeName = ""

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("선택한 위치")
                .font(.headline)

            Text("위도 \(String(format: "%.5f", coordinate.latitude)), 경도 \(String(format: "%.5f", coordinate.longitude))")
                .font(.caption)
                .foregroundStyle(.secondary)

            TextField("장소명을 입력해 주세요", text: $placeName)
                .textInputAutocapitalization(.never)
                .padding(12)
                .background(Color.gray.opacity(0.12))
                .clipShape(RoundedRectangle(cornerRadius: 10))

            Button {
                onAdd(placeName.isEmpty ? "선택한 위치" : placeName)
            } label: {
                if isSubmitting {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Label {
                        Text("후보지 추가")
                    } icon: {
                        Image("plusIcon")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 18, height: 18)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(ZigmaButtonStyle())
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.background)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 12, y: -4)
        .padding()
        .contentShape(Rectangle())
        .onTapGesture {}
    }
}

private struct CandidateActionPanel: View {
    let candidate: CandidatePlace
    let isSubmitting: Bool
    let onVote: () -> Void
    let onDeleteVote: () -> Void
    let onDeleteCandidate: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(candidate.name)
                        .font(.system(size: 22, weight: .semibold))
                        .foregroundStyle(.primary)
                    Text(candidate.address)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)
                    Text("\(candidate.voteInfo.creator.nickname) 님이 제안")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                Text("\(candidate.voteInfo.voteCount)")
                    .font(.title2.bold())
                    .foregroundStyle(Color.zigmaBlue)
            }

            if candidate.isConfirmed {
                Text("확정된 장소")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(.green)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(Color.green.opacity(0.12))
                    .clipShape(Capsule())
            }

            HStack(spacing: 10) {
                Button {
                    candidate.voteInfo.isMyVote ? onDeleteVote() : onVote()
                } label: {
                    Label {
                        Text(candidate.voteInfo.isMyVote ? "투표 취소" : "투표하기")
                    } icon: {
                        Image("voteIcon")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 18, height: 18)
                    }
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(ZigmaButtonStyle(background: candidate.voteInfo.isMyVote ? .gray : Color.zigmaBlue))
                .disabled(candidate.isConfirmed)

                if candidate.voteInfo.isMyCandidate {
                    Button {
                        onDeleteCandidate()
                    } label: {
                        HStack(spacing: 7) {
                            Image("nomineeMinusIcon")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 17, height: 17)
                            Text("후보지 제거")
                                .font(.system(size: 14, weight: .semibold))
                        }
                        .foregroundStyle(.red)
                        .padding(.horizontal, 12)
                        .frame(height: 48)
                        .background(Color.red.opacity(0.10))
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.red.opacity(0.35), lineWidth: 1)
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                    .buttonStyle(.plain)
                    .disabled(candidate.isConfirmed)
                }
            }
            .disabled(isSubmitting)

            if candidate.isConfirmed {
                Text("장소가 확정됐어요\n투표가 종료되었습니다")
                    .font(.caption)
                    .foregroundStyle(.green)
                    .frame(maxWidth: .infinity)
                    .multilineTextAlignment(.center)
                    .padding(.vertical, 6)
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.background)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 12, y: -4)
        .padding()
        .contentShape(Rectangle())
        .onTapGesture {}
    }
}

private struct NativePromiseMapView: UIViewRepresentable {
    @Binding var region: MKCoordinateRegion
    let candidates: [CandidatePlace]
    let comments: [CommentItem]
    let selectedCoordinate: CLLocationCoordinate2D?
    let isCommentMode: Bool
    let onMapTap: (CLLocationCoordinate2D) -> Void
    let onCandidateTap: (CandidatePlace) -> Void
    let onCommentTap: (CommentItem) -> Void
    let onRegionChanged: (MKCoordinateRegion) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.pointOfInterestFilter = .includingAll
        mapView.setRegion(region, animated: false)
        context.coordinator.didSetInitialRegion = true

        let tapGesture = UITapGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handleMapTap(_:)))
        tapGesture.delegate = context.coordinator
        mapView.addGestureRecognizer(tapGesture)

        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        context.coordinator.parent = self
        context.coordinator.syncAnnotations(on: mapView)
    }

    final class Coordinator: NSObject, MKMapViewDelegate, UIGestureRecognizerDelegate {
        var parent: NativePromiseMapView
        var didSetInitialRegion = false

        init(_ parent: NativePromiseMapView) {
            self.parent = parent
        }

        @objc func handleMapTap(_ gesture: UITapGestureRecognizer) {
            guard gesture.state == .ended, let mapView = gesture.view as? MKMapView else { return }
            let point = gesture.location(in: mapView)
            let coordinate = mapView.convert(point, toCoordinateFrom: mapView)
            parent.onMapTap(coordinate)
        }

        func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
            guard let view = touch.view else { return true }
            var currentView: UIView? = view
            while let current = currentView {
                if current is UIControl || current is MKAnnotationView {
                    return false
                }
                currentView = current.superview
            }
            return true
        }

        func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
            parent.onRegionChanged(mapView.region)
        }

        func mapView(_ mapView: MKMapView, didSelect annotation: MKAnnotation) {
            defer { mapView.deselectAnnotation(annotation, animated: false) }
            if let annotation = annotation as? CandidateMapAnnotation {
                parent.onCandidateTap(annotation.candidate)
            } else if let annotation = annotation as? CommentMapAnnotation {
                parent.onCommentTap(annotation.comment)
            }
        }

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if annotation is MKUserLocation {
                return nil
            }

            if let annotation = annotation as? CandidateMapAnnotation {
                let view = mapView.dequeueReusableAnnotationView(withIdentifier: "candidate") ?? MKAnnotationView(annotation: annotation, reuseIdentifier: "candidate")
                view.annotation = annotation
                view.image = UIImage(named: annotation.imageName)
                view.frame = CGRect(x: 0, y: 0, width: 30, height: 30)
                view.centerOffset = CGPoint(x: 0, y: -15)
                view.canShowCallout = false
                return view
            }

            if annotation is SelectedCoordinateAnnotation {
                let view = mapView.dequeueReusableAnnotationView(withIdentifier: "selected") ?? MKAnnotationView(annotation: annotation, reuseIdentifier: "selected")
                view.annotation = annotation
                view.image = UIImage(named: "customMarkerIcon")
                view.frame = CGRect(x: 0, y: 0, width: 30, height: 30)
                view.centerOffset = CGPoint(x: 0, y: -15)
                view.canShowCallout = false
                return view
            }

            if let annotation = annotation as? CommentMapAnnotation {
                let view = mapView.dequeueReusableAnnotationView(withIdentifier: "comment") as? MKMarkerAnnotationView ?? MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: "comment")
                view.annotation = annotation
                view.markerTintColor = UIColor.black.withAlphaComponent(0.78)
                view.glyphText = String(annotation.comment.nickname.prefix(1))
                view.glyphTintColor = .white
                view.canShowCallout = false
                return view
            }

            return nil
        }

        func syncAnnotations(on mapView: MKMapView) {
            let nonUserAnnotations = mapView.annotations.filter { !($0 is MKUserLocation) }
            mapView.removeAnnotations(nonUserAnnotations)

            let candidateAnnotations = parent.candidates.map { candidate in
                CandidateMapAnnotation(candidate: candidate)
            }
            let commentAnnotations = parent.comments.map { comment in
                CommentMapAnnotation(comment: comment)
            }
            mapView.addAnnotations(candidateAnnotations)
            mapView.addAnnotations(commentAnnotations)

            if let selectedCoordinate = parent.selectedCoordinate {
                mapView.addAnnotation(SelectedCoordinateAnnotation(coordinate: selectedCoordinate))
            }
        }
    }
}

private final class CandidateMapAnnotation: NSObject, MKAnnotation {
    let candidate: CandidatePlace
    let coordinate: CLLocationCoordinate2D
    let title: String?
    let imageName: String

    init(candidate: CandidatePlace) {
        self.candidate = candidate
        self.coordinate = CLLocationCoordinate2D(latitude: candidate.latitude, longitude: candidate.longitude)
        self.title = candidate.name
        self.imageName = candidate.isConfirmed || candidate.voteInfo.voteCount > 0 ? "addedMarkerIcon" : "customMarkerIcon"
    }
}

private final class CommentMapAnnotation: NSObject, MKAnnotation {
    let comment: CommentItem
    let coordinate: CLLocationCoordinate2D
    let title: String?

    init(comment: CommentItem) {
        self.comment = comment
        self.coordinate = CLLocationCoordinate2D(latitude: comment.latitude, longitude: comment.longitude)
        self.title = comment.nickname
    }
}

private final class SelectedCoordinateAnnotation: NSObject, MKAnnotation {
    let coordinate: CLLocationCoordinate2D
    let title: String? = "선택한 위치"

    init(coordinate: CLLocationCoordinate2D) {
        self.coordinate = coordinate
    }
}

extension CandidatePlace {
    static func mock(id: Int, name: String, latitude: Double, longitude: Double) -> CandidatePlace {
        CandidatePlace(
            id: id,
            name: name,
            category: "",
            latitude: latitude,
            longitude: longitude,
            address: "위도 \(String(format: "%.5f", latitude)), 경도 \(String(format: "%.5f", longitude))",
            distance: 0,
            isConfirmed: false,
            voteInfo: VoteInfo(
                creator: VoteInfo.User(userId: 1, nickname: "나"),
                voteCount: 0,
                voters: [],
                isMyVote: false,
                isMyCandidate: true
            )
        )
    }

    func withVote(isMyVote: Bool) -> CandidatePlace {
        let nextCount = max(0, voteInfo.voteCount + (isMyVote ? 1 : -1))
        return CandidatePlace(
            id: id,
            name: name,
            category: category,
            latitude: latitude,
            longitude: longitude,
            address: address,
            distance: distance,
            isConfirmed: isConfirmed,
            voteInfo: VoteInfo(
                creator: voteInfo.creator,
                voteCount: nextCount,
                voters: voteInfo.voters,
                isMyVote: isMyVote,
                isMyCandidate: voteInfo.isMyCandidate
            )
        )
    }

    func withConfirmed(_ isConfirmed: Bool) -> CandidatePlace {
        CandidatePlace(
            id: id,
            name: name,
            category: category,
            latitude: latitude,
            longitude: longitude,
            address: address,
            distance: distance,
            isConfirmed: isConfirmed,
            voteInfo: voteInfo
        )
    }

    func withVoteCount(_ voteCount: Int, isMyVote: Bool) -> CandidatePlace {
        CandidatePlace(
            id: id,
            name: name,
            category: category,
            latitude: latitude,
            longitude: longitude,
            address: address,
            distance: distance,
            isConfirmed: isConfirmed,
            voteInfo: VoteInfo(
                creator: voteInfo.creator,
                voteCount: voteCount,
                voters: isMyVote ? voteInfo.voters : [],
                isMyVote: isMyVote,
                isMyCandidate: voteInfo.isMyCandidate
            )
        )
    }
}
