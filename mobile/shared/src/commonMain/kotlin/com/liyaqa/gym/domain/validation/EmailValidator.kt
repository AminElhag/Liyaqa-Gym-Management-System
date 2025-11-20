package com.liyaqa.gym.domain.validation

/**
 * Validates email addresses according to standard format
 */
object EmailValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

    /**
     * Validate email format
     * @param email The email to validate
     * @return ValidationResult indicating whether the email is valid
     */
    fun validate(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid("Email cannot be empty")
            email.length > 254 -> ValidationResult.Invalid("Email is too long (maximum 254 characters)")
            !email.contains("@") -> ValidationResult.Invalid("Email must contain @ symbol")
            !email.matches(EMAIL_REGEX) -> ValidationResult.Invalid("Email format is invalid")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Check if email is valid (simple boolean check)
     */
    fun isValid(email: String): Boolean {
        return validate(email) is ValidationResult.Valid
    }
}

/**
 * Extension function for easy email validation
 */
fun String.isValidEmail(): Boolean {
    return EmailValidator.isValid(this)
}
