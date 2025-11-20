package com.liyaqa.gym.domain.validation

/**
 * Represents the result of a validation operation
 */
sealed class ValidationResult {
    /**
     * Validation passed successfully
     */
    data object Valid : ValidationResult()

    /**
     * Validation failed with an error message
     */
    data class Invalid(val message: String) : ValidationResult()

    /**
     * Check if validation is valid
     */
    fun isValid(): Boolean = this is Valid

    /**
     * Check if validation is invalid
     */
    fun isInvalid(): Boolean = this is Invalid

    /**
     * Get error message if invalid, null otherwise
     */
    fun getErrorOrNull(): String? = when (this) {
        is Valid -> null
        is Invalid -> message
    }

    /**
     * Get error message or throw exception if valid
     */
    fun getError(): String = when (this) {
        is Valid -> throw IllegalStateException("Cannot get error from Valid result")
        is Invalid -> message
    }

    /**
     * Map the error message if invalid
     */
    fun mapError(transform: (String) -> String): ValidationResult {
        return when (this) {
            is Valid -> this
            is Invalid -> Invalid(transform(message))
        }
    }
}

/**
 * Combines multiple validation results
 * Returns Valid only if all results are Valid
 * Returns the first Invalid result encountered
 */
fun combineValidationResults(vararg results: ValidationResult): ValidationResult {
    for (result in results) {
        if (result.isInvalid()) {
            return result
        }
    }
    return ValidationResult.Valid
}

/**
 * Chain multiple validations together
 */
class ValidationChain {
    private val validations = mutableListOf<() -> ValidationResult>()

    /**
     * Add a validation to the chain
     */
    fun add(validation: () -> ValidationResult): ValidationChain {
        validations.add(validation)
        return this
    }

    /**
     * Execute all validations in order
     * Returns the first invalid result or Valid if all pass
     */
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

/**
 * DSL for building validation chains
 */
fun buildValidation(block: ValidationChain.() -> Unit): ValidationResult {
    return ValidationChain().apply(block).validate()
}
