import Foundation
import Combine
import shared

/**
 * LoginViewModel - Handles login authentication logic
 * Uses LoginUseCase from the shared Kotlin module
 */
@MainActor
class LoginViewModel: ObservableObject {
    // MARK: - Published Properties
    @Published var email = ""
    @Published var password = ""
    @Published var isLoading = false
    @Published var isLoggedIn = false
    @Published var errorMessage: String?

    // MARK: - Dependencies
    private let loginUseCase: LoginUseCase
    private let biometricAuthService: BiometricAuthService
    private let tokenStorage: TokenStorage

    // MARK: - Initialization
    init(
        loginUseCase: LoginUseCase? = nil,
        biometricAuthService: BiometricAuthService = .shared,
        tokenStorage: TokenStorage = .shared
    ) {
        // Get LoginUseCase from Koin DI if not provided
        if let loginUseCase = loginUseCase {
            self.loginUseCase = loginUseCase
        } else {
            self.loginUseCase = KoinHelper.shared.getLoginUseCase()
        }
        self.biometricAuthService = biometricAuthService
        self.tokenStorage = tokenStorage
    }

    // MARK: - Login Methods

    /// Login with email and password
    func login(email: String, password: String) async {
        guard validate() else { return }

        isLoading = true
        errorMessage = nil

        do {
            // Call LoginUseCase from shared module
            let result = try await loginUseCase.invoke(email: email, password: password)

            // Handle successful login
            if let loginResponse = result.getOrNull() {
                handleLoginSuccess(loginResponse)
            } else if let error = result.exceptionOrNull() {
                handleLoginError(error)
            }
        } catch {
            handleLoginError(error)
        }

        isLoading = false
    }

    /// Login with email and password (convenience method)
    func login() async {
        await login(email: email, password: password)
    }

    /// Login with biometric authentication (Face ID / Touch ID)
    func loginWithBiometrics() async {
        // Check if biometric authentication is available
        guard biometricAuthService.biometricType != .none else {
            errorMessage = "Biometric authentication is not available on this device"
            return
        }

        // Check if credentials are saved
        guard tokenStorage.hasCredentials() else {
            errorMessage = "No saved credentials for biometric login. Please login with email and password first."
            return
        }

        isLoading = true
        errorMessage = nil

        do {
            // Authenticate with biometrics
            let authenticated = try await biometricAuthService.authenticate()

            if authenticated {
                // Load saved credentials and login
                if let savedEmail = tokenStorage.getEmail(),
                   let savedPassword = tokenStorage.getPassword() {
                    email = savedEmail
                    password = savedPassword
                    await login(email: savedEmail, password: savedPassword)
                }
            } else {
                errorMessage = "Biometric authentication failed"
                isLoading = false
            }
        } catch {
            handleBiometricError(error)
            isLoading = false
        }
    }

    // MARK: - Private Methods

    /// Validate login inputs
    private func validate() -> Bool {
        errorMessage = nil

        // Validate email
        guard !email.isEmpty else {
            errorMessage = "Email is required"
            return false
        }

        guard email.isValidEmail else {
            errorMessage = "Please enter a valid email address"
            return false
        }

        // Validate password
        guard !password.isEmpty else {
            errorMessage = "Password is required"
            return false
        }

        guard password.count >= 8 else {
            errorMessage = "Password must be at least 8 characters"
            return false
        }

        return true
    }

    /// Handle successful login response
    private func handleLoginSuccess(_ loginResponse: LoginResponse) {
        // Save access token
        tokenStorage.save(token: loginResponse.accessToken)

        // Save refresh token
        tokenStorage.saveRefreshToken(loginResponse.refreshToken)

        // Save credentials for biometric login
        tokenStorage.saveCredentials(email: email, password: password)

        // Update state
        isLoggedIn = true

        // Log success for debugging
        print("✅ Login successful for user: \(loginResponse.member.email)")
    }

    /// Handle login error
    private func handleLoginError(_ error: Error) {
        // Parse error message
        let errorDescription = error.localizedDescription

        if errorDescription.contains("401") || errorDescription.contains("Unauthorized") {
            errorMessage = "Invalid email or password"
        } else if errorDescription.contains("Network") || errorDescription.contains("network") {
            errorMessage = "Network error. Please check your connection and try again."
        } else if errorDescription.contains("timeout") {
            errorMessage = "Request timed out. Please try again."
        } else {
            errorMessage = "Login failed. Please try again."
        }

        print("❌ Login error: \(errorDescription)")
    }

    /// Handle biometric authentication error
    private func handleBiometricError(_ error: Error) {
        let nsError = error as NSError

        switch nsError.code {
        case -2: // LAError.userCancel
            errorMessage = "Authentication cancelled"
        case -3: // LAError.userFallback
            errorMessage = "Please use your password to login"
        case -4: // LAError.systemCancel
            errorMessage = "Authentication was cancelled by system"
        case -5: // LAError.passcodeNotSet
            errorMessage = "Passcode is not set on this device"
        case -6: // LAError.biometryNotAvailable
            errorMessage = "Biometric authentication is not available"
        case -7: // LAError.biometryNotEnrolled
            errorMessage = "No biometric credentials enrolled"
        case -8: // LAError.biometryLockout
            errorMessage = "Too many failed attempts. Please try again later."
        default:
            errorMessage = "Biometric authentication failed"
        }

        print("❌ Biometric error: \(error.localizedDescription)")
    }

    /// Clear all form data and errors
    func clearForm() {
        email = ""
        password = ""
        errorMessage = nil
        isLoggedIn = false
    }
}

// MARK: - Email Validation Extension
extension String {
    var isValidEmail: Bool {
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        return emailPredicate.evaluate(with: self)
    }
}
