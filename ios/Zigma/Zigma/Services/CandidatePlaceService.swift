import Foundation
import MapKit

struct CandidatePlaceService {
    var client: APIClient

    func fetchCandidatePlaces(promiseId: Int) async throws -> CandidatePlacesData {
        let response: APIEnvelope<CandidatePlacesData> = try await client.request("promises/\(promiseId)/candidates")
        return response.data
    }

    func addCandidatePlace(promiseId: Int, request: AddCandidatePlaceRequest) async throws -> CandidatePlace {
        let response: APIEnvelope<CandidatePlace> = try await client.request("promises/\(promiseId)/candidates", method: "POST", body: request)
        return response.data
    }

    func deleteCandidatePlace(promiseId: Int, candidateId: Int) async throws {
        let _: APIEnvelope<EmptyPayload?> = try await client.request("promises/\(promiseId)/candidates/\(candidateId)", method: "DELETE")
    }

    func vote(promiseId: Int, candidateId: Int) async throws {
        let request = PostVoteRequest(candidateId: candidateId)
        let _: APIEnvelope<EmptyPayload?> = try await client.request("promises/\(promiseId)/votes", method: "POST", body: request)
    }

    func deleteVote(promiseId: Int, candidateId: Int) async throws {
        let _: APIEnvelope<EmptyPayload?> = try await client.request("promises/\(promiseId)/votes/\(candidateId)", method: "DELETE")
    }

    func confirmPlace(promiseId: Int, candidateId: Int) async throws {
        let request = ConfirmPlaceRequest(candidateId: candidateId)
        let _: APIEnvelope<EmptyPayload?> = try await client.request("promises/\(promiseId)/confirmed", method: "POST", body: request)
    }

    func revote(promiseId: Int) async throws {
        let _: APIEnvelope<EmptyPayload?> = try await client.request("promises/\(promiseId)/revote", method: "POST")
    }
}

struct CommentService {
    var client: APIClient

    func fetchComments(promiseId: Int, region: MKCoordinateRegion) async throws -> CommentListData {
        let minLat = region.center.latitude - region.span.latitudeDelta / 2
        let maxLat = region.center.latitude + region.span.latitudeDelta / 2
        let minLng = region.center.longitude - region.span.longitudeDelta / 2
        let maxLng = region.center.longitude + region.span.longitudeDelta / 2
        let response: APIEnvelope<CommentListData> = try await client.request(
            "promises/\(promiseId)/comments",
            queryItems: [
                URLQueryItem(name: "minLat", value: String(minLat)),
                URLQueryItem(name: "maxLat", value: String(maxLat)),
                URLQueryItem(name: "minLng", value: String(minLng)),
                URLQueryItem(name: "maxLng", value: String(maxLng))
            ]
        )
        return response.data
    }

    func createComment(promiseId: Int, request: CreateCommentRequest) async throws -> CommentItem {
        let response: APIEnvelope<CommentItem> = try await client.request(
            "promises/\(promiseId)/comments",
            method: "POST",
            body: request
        )
        return response.data
    }
}
