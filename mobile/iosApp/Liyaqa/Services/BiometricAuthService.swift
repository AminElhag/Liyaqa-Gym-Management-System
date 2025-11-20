import Foundation
import LocalAuthentication

/**
 * BiometricAuthService - Handles biometric authentication (Face ID / Touch ID)
 * Provides secure device authentication with comprehensive error handling
 */
class BiometricAuthService {
    static let shared = BiometricAuthService()

    private init() {}

    // MARK: - Biometric Type

    enum BiometricType {
        case none
        case touchID
        case faceID

        var displayName: String {
            switch self {
            case .none:
                return "None"
            case .touchID:
                return "Touch ID"
            case .faceID:
                return "Face ID"
            }
        }
    }

    /// Get the available biometric authentication type on the device
    var biometricType: BiometricType {
        let context = LAContext()
        var error: NSError?

        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return .none
        }

        switch context.biometryType {
        case .none:
            return .none
        case .touchID:
            return .touchID
        case .faceID:
            return .faceID
        @unknown default:
            return .none
        }
    }

    /// Check if biometric authentication is available
    var isBiometricAvailable: Bool {
        return biometricType != .none
    }

    // MARK: - Authentication Methods

    /// Authenticate using biometric authentication (Face ID / Touch ID)
    /// - Parameter reason: The reason for authentication shown to the user
    /// - Returns: True if authentication succeeds, false otherwise
    /// - Throws: Authentication errors
    func authenticate(reason: String = "Authenticate to access your account") async throws -> Bool {
        let context = LAContext()

        // Configure context
        context.localizedCancelTitle = "Cancel"
        context.localizedFallbackTitle = "Use Password"

        var error: NSError?

        // Check if biometric authentication is available
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            if let error = error {
                throw BiometricError.from(error)
            }
            throw BiometricError.notAvailable
        }

        // Perform authentication
        return try await withCheckedThrowingContinuation { continuation in
            context.evaluatePolicy(
                .deviceOwnerAuthenticationWithBiometrics,
                localizedReason: reason
            ) { success, error in
                if let error = error {
                    continuation.resume(throwing: BiometricError.from(error as NSError))
                } else {
                    continuation.resume(returning: success)
                }
            }
        }
    }

    /// Authenticate using device passcode as fallback
    /// - Parameter reason: The reason for authentication shown to the user
    /// - Returns: True if authentication succeeds, false otherwise
    /// - Throws: Authentication errors
    func authenticateWithPasscode(reason: String = "Authenticate to access your account") async throws -> Bool {
        let context = LAContext()

        // Configure context
        context.localizedCancelTitle = "Cancel"

        // Perform authentication with passcode fallback
        return try await withCheckedThrowingContinuation { continuation in
            context.evaluatePolicy(
                .deviceOwnerAuthentication,
                localizedReason: reason
            ) { success, error in
                if let error = error {
                    continuation.resume(throwing: BiometricError.from(error as NSError))
                } else {
                    continuation.resume(returning: success)
                }
            }
        }
    }

    /// Authenticate with automatic fallback to passcode if biometric fails
    /// - Parameter reason: The reason for authentication shown to the user
    /// - Returns: True if authentication succeeds (biometric or passcode), false otherwise
    func authenticateWithFallback(reason: String = "Authenticate to access your account") async throws -> Bool {
        do {
            // Try biometric authentication first
            return try await authenticate(reason: reason)
        } catch let error as BiometricError {
            // Handle specific biometric errors
            switch error {
            case .userFallback:
                // User chose to use passcode
                return try await authenticateWithPasscode(reason: reason)
            case .biometryLockout:
                // Too many failed attempts, fallback to passcode
                return try await authenticateWithPasscode(reason: reason)
            default:
                // Re-throw other errors
                throw error
            }
        }
    }
}

// MARK: - Biometric Error

enum BiometricError: LocalizedError {
    case userCancel
    case userFallback
    case systemCancel
    case passcodeNotSet
    case notAvailable
    case notEnrolled
    case biometryLockout
    case appCancel
    case invalidContext
    case unknown(Error)

    var errorDescription: String? {
        switch self {
        case .userCancel:
            return "Authentication was cancelled by user"
        case .userFallback:
            return "User chose to use password"
        case .systemCancel:
            return "Authentication was cancelled by system"
        case .passcodeNotSet:
            return "Passcode is not set on this device"
        case .notAvailable:
            return "Biometric authentication is not available"
        case .notEnrolled:
            return "No biometric credentials are enrolled"
        case .biometryLockout:
            return "Too many failed attempts. Please try again later or use your password."
        case .appCancel:
            return "Authentication was cancelled by app"
        case .invalidContext:
            return "Invalid authentication context"
        case .unknown(let error):
            return error.localizedDescription
        }
    }

    /// Convert LAError to BiometricError
    static func from(_ error: NSError) -> BiometricError {
        switch error.code {
        case LAError.userCancel.rawValue:
            return .userCancel
        case LAError.userFallback.rawValue:
            return .userFallback
        case LAError.systemCancel.rawValue:
            return .systemCancel
        case LAError.passcodeNotSet.rawValue:
            return .passcodeNotSet
        case LAError.biometryNotAvailable.rawValue:
            return .notAvailable
        case LAError.biometryNotEnrolled.rawValue:
            return .notEnrolled
        case LAError.biometryLockout.rawValue:
            return .biometryLockout
        case LAError.appCancel.rawValue:
            return .appCancel
        case LAError.invalidContext.rawValue:
            return .invalidContext
        default:
            return .unknown(error)
        }
    }
}
