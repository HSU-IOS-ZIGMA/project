import Foundation

struct PromiseService {
    var client: APIClient

    func fetchPromises(cursor: String? = nil, size: Int = 20) async throws -> PromiseListData {
        var queryItems = [URLQueryItem(name: "size", value: "\(size)")]
        if let cursor {
            queryItems.append(URLQueryItem(name: "cursor", value: cursor))
        }
        let response: APIEnvelope<PromiseListData> = try await client.request("promises", queryItems: queryItems)
        return response.data
    }

    func fetchPromiseDetail(id: Int) async throws -> PromiseDetail {
        let response: APIEnvelope<PromiseDetail> = try await client.request("promises/\(id)")
        return response.data
    }

    func createPromise(_ request: CreatePromiseRequest) async throws -> PromiseItem {
        let response: APIEnvelope<PromiseItem> = try await client.request("promises", method: "POST", body: request)
        return response.data
    }

    func createInviteCode(promiseId: Int) async throws -> PromiseInviteData {
        let response: APIEnvelope<PromiseInviteData> = try await client.request("promises/\(promiseId)/invite", method: "POST")
        return response.data
    }

    func joinPromise(inviteCode: String) async throws {
        let _: APIEnvelope<EmptyPayload?> = try await client.request("promises/invite/\(inviteCode)", method: "POST")
    }
}
