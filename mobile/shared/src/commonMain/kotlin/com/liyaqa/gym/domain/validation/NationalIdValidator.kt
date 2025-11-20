package com.liyaqa.gym.domain.validation

/**
 * Validates Saudi Arabian National ID (Iqama/National ID)
 * Format: 10 digits
 * First digit indicates type:
 * - 1: Saudi National
 * - 2: Saudi National (alternative)
 */
object NationalIdValidator {
    private const val NATIONAL_ID_LENGTH = 10
    private val NATIONAL_ID_REGEX = Regex("^[12][0-9]{9}\$")

    /**
     * Validate Saudi National ID format
     * @param nationalId The national ID to validate
     * @return ValidationResult indicating whether the national ID is valid
     */
    fun validate(nationalId: String): ValidationResult {
        val cleaned = nationalId.trim()

        return when {
            cleaned.isBlank() -> ValidationResult.Invalid("National ID cannot be empty")
            cleaned.length != NATIONAL_ID_LENGTH ->
                ValidationResult.Invalid("National ID must be exactly $NATIONAL_ID_LENGTH digits")
            !cleaned.all { it.isDigit() } ->
                ValidationResult.Invalid("National ID must contain only digits")
            !cleaned.matches(NATIONAL_ID_REGEX) ->
                ValidationResult.Invalid("Invalid National ID format")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Check if national ID is valid (simple boolean check)
     */
    fun isValid(nationalId: String): Boolean {
        return validate(nationalId) is ValidationResult.Valid
    }

    /**
     * Check if the national ID is for a Saudi national (starts with 1 or 2)
     */
    fun isSaudiNational(nationalId: String): Boolean {
        return isValid(nationalId) && (nationalId.startsWith("1") || nationalId.startsWith("2"))
    }
}

/**
 * Extension function for easy national ID validation
 */
fun String.isValidNationalId(): Boolean {
    return NationalIdValidator.isValid(this)
}

fun String.isSaudiNational(): Boolean {
    return NationalIdValidator.isSaudiNational(this)
}
