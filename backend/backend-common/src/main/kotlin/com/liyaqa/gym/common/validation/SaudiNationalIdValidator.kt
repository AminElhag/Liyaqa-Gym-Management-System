package com.liyaqa.gym.common.validation

/**
 * Validator for Saudi Arabian National ID (Iqama) numbers.
 *
 * Saudi National ID format:
 * - 10 digits long
 * - First digit is the type (1 for Saudi nationals, 2 for residents)
 * - Uses Luhn algorithm for checksum validation
 */
object SaudiNationalIdValidator {

    private val NATIONAL_ID_REGEX = Regex("^[12]\\d{9}$")

    /**
     * Validates Saudi National ID format and checksum.
     *
     * @param nationalId The national ID to validate
     * @return true if the national ID is valid, false otherwise
     */
    fun isValid(nationalId: String?): Boolean {
        if (nationalId.isNullOrBlank()) {
            return false
        }

        // Remove any whitespace
        val cleanedId = nationalId.trim()

        // Check basic format: must be 10 digits starting with 1 or 2
        if (!NATIONAL_ID_REGEX.matches(cleanedId)) {
            return false
        }

        // Validate using Luhn algorithm (mod 10)
        return isValidLuhn(cleanedId)
    }

    /**
     * Validates national ID using the Luhn algorithm (mod 10 checksum).
     *
     * @param nationalId The national ID to validate
     * @return true if the checksum is valid, false otherwise
     */
    private fun isValidLuhn(nationalId: String): Boolean {
        var sum = 0
        var alternate = false

        // Process digits from right to left
        for (i in nationalId.length - 1 downTo 0) {
            var digit = nationalId[i].digitToInt()

            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit = (digit % 10) + 1
                }
            }

            sum += digit
            alternate = !alternate
        }

        return sum % 10 == 0
    }

    /**
     * Checks if the national ID belongs to a Saudi national (starts with 1).
     *
     * @param nationalId The national ID to check
     * @return true if it's a Saudi national ID, false otherwise
     */
    fun isSaudiNational(nationalId: String): Boolean {
        return nationalId.isNotBlank() && nationalId.startsWith('1')
    }

    /**
     * Checks if the national ID belongs to a resident/Iqama holder (starts with 2).
     *
     * @param nationalId The national ID to check
     * @return true if it's a resident ID, false otherwise
     */
    fun isResident(nationalId: String): Boolean {
        return nationalId.isNotBlank() && nationalId.startsWith('2')
    }

    /**
     * Extension function to validate Saudi National ID on String.
     */
    fun String.isValidSaudiNationalId(): Boolean {
        return SaudiNationalIdValidator.isValid(this)
    }

    /**
     * Gets a user-friendly error message for invalid national IDs.
     *
     * @param nationalId The national ID to validate
     * @return Error message if invalid, null if valid
     */
    fun getValidationError(nationalId: String?): String? {
        if (nationalId.isNullOrBlank()) {
            return "National ID cannot be empty"
        }

        val cleanedId = nationalId.trim()

        if (cleanedId.length != 10) {
            return "National ID must be exactly 10 digits long"
        }

        if (!cleanedId.all { it.isDigit() }) {
            return "National ID must contain only digits"
        }

        if (!cleanedId.startsWith('1') && !cleanedId.startsWith('2')) {
            return "National ID must start with 1 (Saudi national) or 2 (resident)"
        }

        if (!isValidLuhn(cleanedId)) {
            return "Invalid National ID checksum"
        }

        return null
    }
}

/**
 * Extension function to validate Saudi National ID on String?.
 */
fun String?.isValidSaudiNationalId(): Boolean {
    return SaudiNationalIdValidator.isValid(this)
}
