import Foundation

struct CreatePromiseRequest: Encodable {
    let title: String
    let promisedAt: String
    let category: String
    let endAt: String?
    let isMultipleVoting: Bool?
}

struct PromiseItem: Identifiable, Decodable {
    let id: Int
    let title: String
    let promiseStatus: String
    let promisedAt: String
    let dayOfWeek: String
    let memberCount: Int
    let isLeader: Bool

    var isConfirmed: Bool {
        promiseStatus == "확정 완료" || promiseStatus == "CONFIRMED"
    }
}

struct PromiseListData: Decodable {
    let promises: [PromiseItem]
    let cursor: String?
    let count: Int
    let hasNext: Bool
}

struct PromiseMember: Identifiable, Decodable {
    let userId: Int
    let nickName: String
    let profileImageUrl: String?
    let role: String
    let isSelf: Bool

    var id: Int { userId }
}

struct PromiseDetail: Identifiable, Decodable {
    let id: Int
    let title: String
    let promisedAt: String
    let dayOfWeek: String
    let isMultipleVoting: Bool
    let memberCount: Int
    let members: [PromiseMember]
    let isLeader: Bool
    let promiseStatus: String
}

struct PromiseInviteData: Decodable {
    let promiseId: Int
    let inviteCode: String
}
