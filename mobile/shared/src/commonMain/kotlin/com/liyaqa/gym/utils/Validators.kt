package com.liyaqa.gym.utils

/**
 * Validation utilities for common input validation
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    private val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}\$")

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && email.matches(EMAIL_REGEX)
    }

    /**
     * Validate phone number format
     */
    fun isValidPhone(phone: String): Boolean {
        return phone.isNotBlank() && phone.matches(PHONE_REGEX)
    }

    /**
     * Validate that string is not blank
     */
    fun isNotBlank(value: String?): Boolean {
        return !value.isNullOrBlank()
    }

    /**
     * Validate minimum length
     */
    fun hasMinLength(value: String?, minLength: Int): Boolean {
        return value != null && value.length >= minLength
    }

    /**
     * Validate maximum length
     */
    fun hasMaxLength(value: String?, maxLength: Int): Boolean {
        return value != null && value.length <= maxLength
    }

    /**
     * Validate length range
     */
    fun hasLengthInRange(value: String?, minLength: Int, maxLength: Int): Boolean {
        return value != null && value.length in minLength..maxLength
    }

    /**
     * Validate that value is positive
     */
    fun isPositive(value: Int): Boolean {
        return value > 0
    }

    /**
     * Validate that value is non-negative
     */
    fun isNonNegative(value: Int): Boolean {
        return value >= 0
    }

    /**
     * Validate that value is in range
     */
    fun isInRange(value: Int, min: Int, max: Int): Boolean {
        return value in min..max
    }
}

/**
 * Validation result
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()

    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = this is Invalid

    fun getErrorOrNull(): String? = when (this) {
        is Valid -> null
        is Invalid -> message
    }
}

/**
 * Chain multiple validations
 */
class ValidationChain {
    private val validations = mutableListOf<() -> ValidationResult>()

    fun add(validation: () -> ValidationResult): ValidationChain {
        validations.add(validation)
        return this
    }

    fun validate(): ValidationResult {
        for (validation in validations) {
            val result = validation()
            if (result.isInvalid()) {
                return result
            }
        }
        return ValidationResult.Valid
    }
}
