import Foundation
import Combine
import shared

@MainActor
class AuthViewModel: ObservableObject {
    @Published var email = ""
    @Published var password = ""
    @Published var fullName = ""
    @Published var phoneNumber = ""
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var isAuthenticated = false

    private let authService: AuthService
    private var cancellables = Set<AnyCancellable>()

    init(authService: AuthService = .shared) {
        self.authService = authService
    }

    func login() async {
        guard validate() else { return }

        isLoading = true
        errorMessage = nil

        do {
            let result = try await authService.login(email: email, password: password)
            isAuthenticated = true
            isLoading = false
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
        }
    }

    func register() async {
        guard validateRegistration() else { return }

        isLoading = true
        errorMessage = nil

        do {
            let result = try await authService.register(
                email: email,
                password: password,
                fullName: fullName,
                phoneNumber: phoneNumber
            )
            isAuthenticated = true
            isLoading = false
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
        }
    }

    func loginWithBiometrics() async {
        isLoading = true
        errorMessage = nil

        do {
            let result = try await BiometricAuthService.shared.authenticate()
            if result {
                // Load saved credentials and login
                if let savedEmail = KeychainHelper.shared.getEmail(),
                   let savedPassword = KeychainHelper.shared.getPassword() {
                    email = savedEmail
                    password = savedPassword
                    await login()
                }
            }
        } catch {
            errorMessage = "Biometric authentication failed"
            isLoading = false
        }
    }

    private func validate() -> Bool {
        errorMessage = nil

        guard !email.isEmpty else {
            errorMessage = "Email is required"
            return false
        }

        guard email.isValidEmail else {
            errorMessage = "Invalid email format"
            return false
        }

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

    private func validateRegistration() -> Bool {
        guard validate() else { return false }

        guard !fullName.isEmpty else {
            errorMessage = "Full name is required"
            return false
        }

        if !phoneNumber.isEmpty && !phoneNumber.isValidPhoneNumber {
            errorMessage = "Invalid phone number"
            return false
        }

        return true
    }

    func resetPassword() async {
        guard !email.isEmpty, email.isValidEmail else {
            errorMessage = "Please enter a valid email"
            return
        }

        isLoading = true
        errorMessage = nil

        do {
            try await authService.resetPassword(email: email)
            errorMessage = "Password reset link sent to your email"
            isLoading = false
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
        }
    }
}

// MARK: - Email Validation
extension String {
    var isValidEmail: Bool {
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        return emailPredicate.evaluate(with: self)
    }

    var isValidPhoneNumber: Bool {
        let phoneRegex = "^[+]?[0-9]{10,15}$"
        let phonePredicate = NSPredicate(format: "SELF MATCHES %@", phoneRegex)
        return phonePredicate.evaluate(with: self)
    }
}
