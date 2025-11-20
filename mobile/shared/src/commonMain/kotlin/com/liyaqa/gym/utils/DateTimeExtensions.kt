package com.liyaqa.gym.utils

import kotlinx.datetime.*

/**
 * Extension functions for DateTime operations
 */

/**
 * Format LocalDate to ISO string
 */
fun LocalDate.toIsoString(): String {
    return this.toString()
}

/**
 * Format Instant to ISO string
 */
fun Instant.toIsoString(): String {
    return this.toString()
}

/**
 * Get current date in the system timezone
 */
fun Clock.todayIn(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
    return this.now().toLocalDateTime(timeZone).date
}

/**
 * Get current date and time in the system timezone
 */
fun Clock.nowIn(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
    return this.now().toLocalDateTime(timeZone)
}

/**
 * Convert string to LocalDate
 */
fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert string to Instant
 */
fun String.toInstantOrNull(): Instant? {
    return try {
        Instant.parse(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert string to LocalDateTime
 */
fun String.toLocalDateTimeOrNull(): LocalDateTime? {
    return try {
        LocalDateTime.parse(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * Check if a LocalDate is today
 */
fun LocalDate.isToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    return this == Clock.System.todayIn(timeZone)
}

/**
 * Check if a LocalDateTime is in the past
 */
fun LocalDateTime.isPast(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val now = Clock.System.nowIn(timeZone)
    return this < now
}

/**
 * Check if a LocalDateTime is in the future
 */
fun LocalDateTime.isFuture(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val now = Clock.System.nowIn(timeZone)
    return this > now
}

/**
 * Format LocalDate for display (e.g., "Jan 15, 2024")
 */
fun LocalDate.toDisplayString(): String {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return "${monthNames[monthNumber - 1]} $dayOfMonth, $year"
}

/**
 * Format LocalDateTime for display (e.g., "Jan 15, 2024 at 2:30 PM")
 */
fun LocalDateTime.toDisplayString(): String {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val minuteStr = minute.toString().padStart(2, '0')
    return "${monthNames[monthNumber - 1]} $dayOfMonth, $year at $displayHour:$minuteStr $amPm"
}

/**
 * Format time only (e.g., "2:30 PM")
 */
fun LocalDateTime.toTimeString(): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val minuteStr = minute.toString().padStart(2, '0')
    return "$displayHour:$minuteStr $amPm"
}
