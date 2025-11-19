package com.liyaqa.gym.presentation.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Custom validation annotation for Saudi National ID.
 * Validates that the ID is in the correct format (10 digits starting with 1 or 2)
 * and passes Luhn checksum validation.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SaudiNationalIdValidator::class])
@MustBeDocumented
annotation class SaudiNationalId(
    val message: String = "Invalid Saudi National ID format. Must be 10 digits starting with 1 or 2 and pass checksum validation.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
