import Foundation

struct VoteInfo: Decodable {
    struct User: Decodable, Identifiable {
        let userId: Int
        let nickname: String
        var id: Int { userId }
    }

    let creator: User
    let voteCount: Int
    let voters: [User]
    let isMyVote: Bool
    let isMyCandidate: Bool
}

struct CandidatePlace: Identifiable, Decodable {
    let id: Int
    let name: String
    let category: String
    let latitude: Double
    let longitude: Double
    let address: String
    let distance: Double
    let isConfirmed: Bool
    let voteInfo: VoteInfo
}

struct CandidatePlacesData: Decodable {
    let candidates: [CandidatePlace]
    let candidateCount: Int
    let totalMemberCount: Int
}

struct AddCandidatePlaceRequest: Encodable {
    let name: String
    let address: String
    let latitude: Double
    let longitude: Double
    let category: String
}

struct PostVoteRequest: Encodable {
    let candidateId: Int
}

struct ConfirmPlaceRequest: Encodable {
    let candidateId: Int
}

struct PresenceMember: Identifiable, Decodable {
    let userId: Int
    let nickName: String
    let profileImageUrl: String?
    var id: Int { userId }
}

struct CommentItem: Identifiable, Decodable {
    let id: Int
    let userId: Int
    let nickname: String
    let profileImageUrl: String?
    let content: String
    let latitude: Double
    let longitude: Double
    let createdAt: String
}

struct CommentListData: Decodable {
    let comments: [CommentItem]
    let count: Int
}

struct CreateCommentRequest: Encodable {
    let content: String
    let latitude: Double
    let longitude: Double
}
