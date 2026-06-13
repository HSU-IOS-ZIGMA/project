import Foundation

struct APIEnvelope<T: Decodable>: Decodable {
    let isSuccess: Bool
    let timestamp: String
    let code: String
    let httpStatus: Int
    let message: String
    let data: T
}

struct EmptyPayload: Decodable {}

