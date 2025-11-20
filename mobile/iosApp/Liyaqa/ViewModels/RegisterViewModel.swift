import Foundation
import Combine
import shared

/**
 * RegisterViewModel - Handles user registration logic
 * Uses RegisterUseCase from the shared Kotlin module
 */
@MainActor
class RegisterViewModel: ObservableObject {
    // MARK: - Published Properties
    @Published var fullName = ""
    @Published var email = ""
    @Published var phoneNumber = ""
    @Published var nationalId = ""
    @Published var password = ""
    @Published var isLoading = false
    @Published var isRegistered = false
    @Published var errorMessage: String?

    // MARK: - Validation States
    @Published var isPasswordValid = false
    @Published var passwordValidationMessage = ""

    // MARK: - Dependencies
    private let registerUseCase: RegisterUseCase
    private let tokenStorage: TokenStorage

    // MARK: - Initialization
    init(
        registerUseCase: RegisterUseCase? = nil,
        tokenStorage: TokenStorage = .shared
    ) {
        // Get RegisterUseCase from Koin DI if not provided
        if let registerUseCase = registerUseCase {
            self.registerUseCase = registerUseCase
        } else {
            self.registerUseCase = KoinHelper.shared.getRegisterUseCase()
        }
        self.tokenStorage = tokenStorage
    }

    // MARK: - Registration Methods

    /// Register a new user account
    func register() async {
        guard validateAll() else { return }

        isLoading = true
        errorMessage = nil

        do {
            // Call RegisterUseCase from shared module
            let result = try await registerUseCase.invoke(
                name: fullName,
                email: email,
                phone: phoneNumber,
                password: password,
                nationalId: nationalId
            )

            // Handle successful registration
            if let loginResponse = result.getOrNull() {
                handleRegistrationSuccess(loginResponse)
            } else if let error = result.exceptionOrNull() {
                handleRegistrationError(error)
            }
        } catch {
            handleRegistrationError(error)
        }

        isLoading = false
    }

    // MARK: - Validation Methods

    /// Validate all registration inputs
    private func validateAll() -> Bool {
        errorMessage = nil

        // Validate full name
        guard !fullName.isEmpty else {
            errorMessage = "Full name is required"
            return false
        }

        guard fullName.count >= 2 else {
            errorMessage = "Full name must be at least 2 characters"
            return false
        }

        // Validate email
        guard !email.isEmpty else {
            errorMessage = "Email is required"
            return false
        }

        guard email.isValidEmail else {
            errorMessage = "Please enter a valid email address"
            return false
        }

        // Validate phone number
        guard !phoneNumber.isEmpty else {
            errorMessage = "Phone number is required"
            return false
        }

        guard phoneNumber.isValidSaudiPhoneNumber else {
            errorMessage = "Please enter a valid Saudi phone number (e.g., 05xxxxxxxx or +9665xxxxxxxx)"
            return false
        }

        // Validate national ID
        guard !nationalId.isEmpty else {
            errorMessage = "National ID is required"
            return false
        }

        guard nationalId.isValidSaudiNationalId else {
            errorMessage = "Please enter a valid Saudi National ID (10 digits)"
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

    /// Validate password and update validation state
    func validatePassword() {
        if password.isEmpty {
            isPasswordValid = false
            passwordValidationMessage = ""
            return
        }

        let validations: [(Bool, String)] = [
            (password.count >= 8, "At least 8 characters"),
            (password.containsUppercase, "One uppercase letter"),
            (password.containsLowercase, "One lowercase letter"),
            (password.containsNumber, "One number")
        ]

        let failedValidations = validations.filter { !$0.0 }.map { $0.1 }

        if failedValidations.isEmpty {
            isPasswordValid = true
            passwordValidationMessage = "Strong password"
        } else {
            isPasswordValid = false
            passwordValidationMessage = "Password needs: \(failedValidations.joined(separator: ", "))"
        }
    }

    // MARK: - Private Methods

    /// Handle successful registration response
    private func handleRegistrationSuccess(_ loginResponse: LoginResponse) {
        // Save access token
        tokenStorage.save(token: loginResponse.accessToken)

        // Save refresh token
        tokenStorage.saveRefreshToken(loginResponse.refreshToken)

        // Save credentials for biometric login
        tokenStorage.saveCredentials(email: email, password: password)

        // Update state
        isRegistered = true

        // Log success for debugging
        print("✅ Registration successful for user: \(loginResponse.member.email)")
    }

    /// Handle registration error
    private func handleRegistrationError(_ error: Error) {
        // Parse error message
        let errorDescription = error.localizedDescription

        if errorDescription.contains("409") || errorDescription.contains("already exists") {
            errorMessage = "An account with this email already exists"
        } else if errorDescription.contains("400") || errorDescription.contains("Bad Request") {
            errorMessage = "Invalid registration information. Please check your details."
        } else if errorDescription.contains("Network") || errorDescription.contains("network") {
            errorMessage = "Network error. Please check your connection and try again."
        } else if errorDescription.contains("timeout") {
            errorMessage = "Request timed out. Please try again."
        } else if errorDescription.contains("Name cannot be empty") {
            errorMessage = "Please enter your full name"
        } else if errorDescription.contains("email") {
            errorMessage = errorDescription
        } else if errorDescription.contains("phone") {
            errorMessage = errorDescription
        } else if errorDescription.contains("National ID") {
            errorMessage = errorDescription
        } else {
            errorMessage = "Registration failed. Please try again."
        }

        print("❌ Registration error: \(errorDescription)")
    }

    /// Clear all form data and errors
    func clearForm() {
        fullName = ""
        email = ""
        phoneNumber = ""
        nationalId = ""
        password = ""
        errorMessage = nil
        isRegistered = false
        isPasswordValid = false
        passwordValidationMessage = ""
    }

    /// Format phone number as user types
    func formatPhoneNumber() {
        // Remove any non-digit characters
        let digits = phoneNumber.filter { $0.isNumber }

        // Format based on Saudi phone number format
        if digits.hasPrefix("966") {
            phoneNumber = "+\(digits)"
        } else if digits.hasPrefix("05") && digits.count <= 10 {
            phoneNumber = digits
        } else if digits.hasPrefix("5") && digits.count <= 9 {
            phoneNumber = "0\(digits)"
        } else {
            phoneNumber = digits
        }
    }
}

// MARK: - Validation Extensions
extension String {
    var isValidEmail: Bool {
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        return emailPredicate.evaluate(with: self)
    }

    var isValidSaudiPhoneNumber: Bool {
        // Remove any non-digit characters
        let digits = self.filter { $0.isNumber }

        // Check for valid Saudi phone number formats:
        // 1. 05xxxxxxxx (10 digits starting with 05)
        // 2. 9665xxxxxxx (12 digits starting with 966)
        // 3. +9665xxxxxxx (with +)

        if digits.hasPrefix("05") && digits.count == 10 {
            return true
        } else if digits.hasPrefix("9665") && digits.count == 12 {
            return true
        } else if self.hasPrefix("+966") && digits.count == 12 {
            return true
        }

        return false
    }

    var isValidSaudiNationalId: Bool {
        // Saudi National ID must be exactly 10 digits
        let digits = self.filter { $0.isNumber }

        // Must be exactly 10 digits
        guard digits.count == 10 else { return false }

        // First digit must be 1 or 2 (1 for Saudi, 2 for resident)
        guard let firstDigit = digits.first, firstDigit == "1" || firstDigit == "2" else {
            return false
        }

        return true
    }

    var containsUppercase: Bool {
        return self.rangeOfCharacter(from: .uppercaseLetters) != nil
    }

    var containsLowercase: Bool {
        return self.rangeOfCharacter(from: .lowercaseLetters) != nil
    }

    var containsNumber: Bool {
        return self.rangeOfCharacter(from: .decimalDigits) != nil
    }
}
