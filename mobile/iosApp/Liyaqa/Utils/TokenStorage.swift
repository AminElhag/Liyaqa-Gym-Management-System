import Foundation

/**
 * TokenStorage - Secure token management utility
 * Provides a simple interface for storing and retrieving authentication tokens
 * Uses Keychain for secure storage
 */
class TokenStorage {
    static let shared = TokenStorage()

    private let keychainHelper = KeychainHelper.shared

    private init() {}

    // MARK: - Token Management

    /// Save access token securely in Keychain
    /// - Parameter token: The access token to save
    func save(token: String) {
        keychainHelper.saveToken(token)
    }

    /// Retrieve the saved access token from Keychain
    /// - Returns: The access token if available, nil otherwise
    func getToken() -> String? {
        return keychainHelper.getToken()
    }

    /// Delete the saved access token from Keychain
    func deleteToken() {
        keychainHelper.deleteToken()
    }

    /// Check if a valid token exists
    /// - Returns: true if a token exists in storage, false otherwise
    func hasToken() -> Bool {
        return getToken() != nil
    }

    // MARK: - Refresh Token Management

    /// Save refresh token securely in Keychain
    /// - Parameter refreshToken: The refresh token to save
    func saveRefreshToken(_ refreshToken: String) {
        keychainHelper.save(key: "refresh_token", value: refreshToken)
    }

    /// Retrieve the saved refresh token from Keychain
    /// - Returns: The refresh token if available, nil otherwise
    func getRefreshToken() -> String? {
        return keychainHelper.get(key: "refresh_token")
    }

    /// Delete the saved refresh token from Keychain
    func deleteRefreshToken() {
        keychainHelper.delete(key: "refresh_token")
    }

    // MARK: - Credentials Management (for biometric login)

    /// Save user credentials securely for biometric authentication
    /// - Parameters:
    ///   - email: User's email address
    ///   - password: User's password
    func saveCredentials(email: String, password: String) {
        keychainHelper.saveCredentials(email: email, password: password)
    }

    /// Retrieve saved email from Keychain
    /// - Returns: The saved email if available, nil otherwise
    func getEmail() -> String? {
        return keychainHelper.getEmail()
    }

    /// Retrieve saved password from Keychain
    /// - Returns: The saved password if available, nil otherwise
    func getPassword() -> String? {
        return keychainHelper.getPassword()
    }

    /// Delete saved credentials from Keychain
    func deleteCredentials() {
        keychainHelper.deleteCredentials()
    }

    /// Check if credentials are saved (for biometric login)
    /// - Returns: true if both email and password are saved, false otherwise
    func hasCredentials() -> Bool {
        return getEmail() != nil && getPassword() != nil
    }

    // MARK: - Clear All

    /// Clear all authentication data from secure storage
    func clearAll() {
        deleteToken()
        deleteRefreshToken()
        deleteCredentials()
    }
}

// MARK: - KeychainHelper Extension
extension KeychainHelper {
    func save(key: String, value: String) {
        guard let data = value.data(using: .utf8) else { return }

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlocked
        ]

        // Delete any existing item
        SecItemDelete(query as CFDictionary)

        // Add new item
        let status = SecItemAdd(query as CFDictionary, nil)
        if status != errSecSuccess {
            print("Error saving to keychain: \(status)")
        }
    }

    func get(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess,
              let data = result as? Data,
              let value = String(data: data, encoding: .utf8) else {
            return nil
        }

        return value
    }

    func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]

        SecItemDelete(query as CFDictionary)
    }
}
