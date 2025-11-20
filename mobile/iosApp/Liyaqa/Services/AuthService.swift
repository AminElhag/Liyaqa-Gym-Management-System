import Foundation
import shared

class AuthService {
    static let shared = AuthService()

    private init() {}

    func login(email: String, password: String) async throws -> User {
        // TODO: Integrate with shared Kotlin repository
        // For now, simulate API call
        try await Task.sleep(nanoseconds: 1_000_000_000)

        // Mock user
        let user = User(
            id: UUID().uuidString,
            email: email,
            fullName: "Test User",
            phoneNumber: nil,
            profileImageUrl: nil,
            membershipStatus: "active",
            membershipExpiry: Date().addingTimeInterval(30*24*60*60)
        )

        // Save token
        KeychainHelper.shared.saveToken("mock_token_\(UUID().uuidString)")

        // Save credentials for biometric login
        KeychainHelper.shared.saveCredentials(email: email, password: password)

        return user
    }

    func register(email: String, password: String, fullName: String, phoneNumber: String?) async throws -> User {
        // TODO: Integrate with shared Kotlin repository
        try await Task.sleep(nanoseconds: 1_000_000_000)

        let user = User(
            id: UUID().uuidString,
            email: email,
            fullName: fullName,
            phoneNumber: phoneNumber,
            profileImageUrl: nil,
            membershipStatus: "active",
            membershipExpiry: Date().addingTimeInterval(30*24*60*60)
        )

        KeychainHelper.shared.saveToken("mock_token_\(UUID().uuidString)")
        KeychainHelper.shared.saveCredentials(email: email, password: password)

        return user
    }

    func resetPassword(email: String) async throws {
        // TODO: Integrate with shared Kotlin repository
        try await Task.sleep(nanoseconds: 1_000_000_000)
    }

    func logout() {
        KeychainHelper.shared.deleteToken()
        KeychainHelper.shared.deleteCredentials()
    }
}
