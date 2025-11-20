package com.liyaqa.gym.domain.validation

import kotlinx.datetime.*

/**
 * Validates date ranges and date-related business rules
 */
object DateRangeValidator {

    /**
     * Validate that start date is before end date
     */
    fun validateRange(startDate: LocalDate, endDate: LocalDate): ValidationResult {
        return when {
            startDate > endDate ->
                ValidationResult.Invalid("Start date must be before or equal to end date")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate that a date is in the future
     */
    fun validateFutureDate(date: LocalDate, referenceDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): ValidationResult {
        return when {
            date < referenceDate ->
                ValidationResult.Invalid("Date must be in the future")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate that a date is in the past
     */
    fun validatePastDate(date: LocalDate, referenceDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): ValidationResult {
        return when {
            date > referenceDate ->
                ValidationResult.Invalid("Date must be in the past")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate that a date is within a specific range
     */
    fun validateWithinRange(
        date: LocalDate,
        minDate: LocalDate,
        maxDate: LocalDate
    ): ValidationResult {
        return when {
            date < minDate ->
                ValidationResult.Invalid("Date must be on or after ${minDate}")
            date > maxDate ->
                ValidationResult.Invalid("Date must be on or before ${maxDate}")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate that a date time is in the future
     */
    fun validateFutureDateTime(
        dateTime: LocalDateTime,
        referenceDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): ValidationResult {
        return when {
            dateTime < referenceDateTime ->
                ValidationResult.Invalid("Date and time must be in the future")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate that a date time is in the past
     */
    fun validatePastDateTime(
        dateTime: LocalDateTime,
        referenceDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): ValidationResult {
        return when {
            dateTime > referenceDateTime ->
                ValidationResult.Invalid("Date and time must be in the past")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate minimum age requirement
     * @param dateOfBirth The date of birth
     * @param minAge Minimum age required
     * @param referenceDate Date to calculate age from (defaults to today)
     */
    fun validateMinimumAge(
        dateOfBirth: LocalDate,
        minAge: Int,
        referenceDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ): ValidationResult {
        val age = calculateAge(dateOfBirth, referenceDate)
        return when {
            age < minAge ->
                ValidationResult.Invalid("Must be at least $minAge years old")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate booking window (e.g., must book at least X hours in advance)
     */
    fun validateBookingWindow(
        scheduleDateTime: LocalDateTime,
        minHoursInAdvance: Int,
        referenceDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): ValidationResult {
        val hoursDifference = (scheduleDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds -
                               referenceDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds) / 3600

        return when {
            hoursDifference < minHoursInAdvance ->
                ValidationResult.Invalid("Must book at least $minHoursInAdvance hours in advance")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate cancellation window (e.g., must cancel at least X hours before class)
     */
    fun validateCancellationWindow(
        scheduleDateTime: LocalDateTime,
        minHoursBeforeClass: Int,
        referenceDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): ValidationResult {
        val hoursDifference = (scheduleDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds -
                               referenceDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds) / 3600

        return when {
            hoursDifference < minHoursBeforeClass ->
                ValidationResult.Invalid("Must cancel at least $minHoursBeforeClass hours before class")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Calculate age from date of birth
     */
    private fun calculateAge(dateOfBirth: LocalDate, referenceDate: LocalDate): Int {
        var age = referenceDate.year - dateOfBirth.year
        if (referenceDate.monthNumber < dateOfBirth.monthNumber ||
            (referenceDate.monthNumber == dateOfBirth.monthNumber && referenceDate.dayOfMonth < dateOfBirth.dayOfMonth)
        ) {
            age--
        }
        return age
    }
}

/**
 * Extension functions for date validation
 */
fun LocalDate.isFuture(): Boolean {
    return this > Clock.System.todayIn(TimeZone.currentSystemDefault())
}

fun LocalDate.isPast(): Boolean {
    return this < Clock.System.todayIn(TimeZone.currentSystemDefault())
}

fun LocalDateTime.isFuture(): Boolean {
    return this > Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

fun LocalDateTime.isPast(): Boolean {
    return this < Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}
