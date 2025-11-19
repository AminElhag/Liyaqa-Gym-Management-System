package com.liyaqa.gym.presentation.validation

import com.liyaqa.gym.common.validation.isValidSaudiNationalId
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * Validator implementation for @SaudiNationalId annotation.
 * Uses the common SaudiNationalIdValidator for the actual validation logic.
 */
class SaudiNationalIdValidator : ConstraintValidator<SaudiNationalId, String?> {

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        // Null or empty values are considered valid (use @NotBlank/@NotNull for required fields)
        if (value.isNullOrBlank()) {
            return true
        }

        // Use the common validator for the actual validation logic
        return value.isValidSaudiNationalId()
    }
}
