package com.liyaqa.gym.domain.validation

/**
 * Validates phone numbers with focus on Saudi Arabian format
 * Accepts formats:
 * - +966512345678 (International format)
 * - 0512345678 (Local format)
 * - 512345678 (Without prefix)
 */
object PhoneValidator {
    // Saudi phone number patterns
    private val SAUDI_MOBILE_WITH_COUNTRY_CODE = Regex("^\\+9665[0-9]{8}\$")
    private val SAUDI_MOBILE_WITH_LOCAL_PREFIX = Regex("^05[0-9]{8}\$")
    private val SAUDI_MOBILE_WITHOUT_PREFIX = Regex("^5[0-9]{8}\$")

    // General international format
    private val INTERNATIONAL_PHONE = Regex("^\\+?[1-9][0-9]{7,14}\$")

    /**
     * Validate Saudi Arabian phone number
     * @param phone The phone number to validate
     * @return ValidationResult indicating whether the phone is valid
     */
    fun validateSaudi(phone: String): ValidationResult {
        val cleaned = phone.replace("\\s+".toRegex(), "").replace("-", "")

        return when {
            cleaned.isBlank() -> ValidationResult.Invalid("Phone number cannot be empty")
            cleaned.matches(SAUDI_MOBILE_WITH_COUNTRY_CODE) -> ValidationResult.Valid
            cleaned.matches(SAUDI_MOBILE_WITH_LOCAL_PREFIX) -> ValidationResult.Valid
            cleaned.matches(SAUDI_MOBILE_WITHOUT_PREFIX) -> ValidationResult.Valid
            else -> ValidationResult.Invalid("Invalid Saudi phone number format. Expected: +966XXXXXXXXX, 05XXXXXXXX, or 5XXXXXXXX")
        }
    }

    /**
     * Validate phone number (international format)
     * @param phone The phone number to validate
     * @return ValidationResult indicating whether the phone is valid
     */
    fun validate(phone: String): ValidationResult {
        val cleaned = phone.replace("\\s+".toRegex(), "").replace("-", "")

        return when {
            cleaned.isBlank() -> ValidationResult.Invalid("Phone number cannot be empty")
            cleaned.length < 8 -> ValidationResult.Invalid("Phone number is too short")
            cleaned.length > 15 -> ValidationResult.Invalid("Phone number is too long")
            !cleaned.matches(INTERNATIONAL_PHONE) -> ValidationResult.Invalid("Invalid phone number format")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Normalize Saudi phone number to international format
     * @param phone The phone number to normalize
     * @return Normalized phone number starting with +966, or null if invalid
     */
    fun normalizeSaudi(phone: String): String? {
        val cleaned = phone.replace("\\s+".toRegex(), "").replace("-", "")

        return when {
            cleaned.matches(SAUDI_MOBILE_WITH_COUNTRY_CODE) -> cleaned
            cleaned.matches(SAUDI_MOBILE_WITH_LOCAL_PREFIX) -> "+966${cleaned.substring(1)}"
            cleaned.matches(SAUDI_MOBILE_WITHOUT_PREFIX) -> "+966$cleaned"
            else -> null
        }
    }

    /**
     * Check if phone is valid (simple boolean check)
     */
    fun isValid(phone: String): Boolean {
        return validate(phone) is ValidationResult.Valid
    }

    /**
     * Check if Saudi phone is valid (simple boolean check)
     */
    fun isValidSaudi(phone: String): Boolean {
        return validateSaudi(phone) is ValidationResult.Valid
    }
}

/**
 * Extension functions for easy phone validation
 */
fun String.isValidPhone(): Boolean {
    return PhoneValidator.isValid(this)
}

fun String.isValidSaudiPhone(): Boolean {
    return PhoneValidator.isValidSaudi(this)
}

fun String.normalizeSaudiPhone(): String? {
    return PhoneValidator.normalizeSaudi(this)
}
