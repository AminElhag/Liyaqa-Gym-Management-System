import Foundation
import Security

class KeychainHelper {
    static let shared = KeychainHelper()

    private init() {}

    // MARK: - Token Management
    func saveToken(_ token: String) {
        save(key: "auth_token", value: token)
    }

    func getToken() -> String? {
        return get(key: "auth_token")
    }

    func deleteToken() {
        delete(key: "auth_token")
    }

    // MARK: - Credentials Management
    func saveCredentials(email: String, password: String) {
        save(key: "user_email", value: email)
        save(key: "user_password", value: password)
    }

    func getEmail() -> String? {
        return get(key: "user_email")
    }

    func getPassword() -> String? {
        return get(key: "user_password")
    }

    func deleteCredentials() {
        delete(key: "user_email")
        delete(key: "user_password")
    }

    // MARK: - Generic Keychain Operations
    private func save(key: String, value: String) {
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

    private func get(key: String) -> String? {
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

    private func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]

        SecItemDelete(query as CFDictionary)
    }

    func clearAll() {
        let secItemClasses = [
            kSecClassGenericPassword,
            kSecClassInternetPassword,
            kSecClassCertificate,
            kSecClassKey,
            kSecClassIdentity
        ]

        for itemClass in secItemClasses {
            let query: [String: Any] = [kSecClass as String: itemClass]
            SecItemDelete(query as CFDictionary)
        }
    }
}
