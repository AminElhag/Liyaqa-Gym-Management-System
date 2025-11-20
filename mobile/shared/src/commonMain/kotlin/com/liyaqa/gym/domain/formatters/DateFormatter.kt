package com.liyaqa.gym.domain.formatters

import kotlinx.datetime.*

/**
 * Platform-specific date formatter interface
 * Implementations should use platform-native date formatting
 */
expect class DateFormatter() {
    /**
     * Format a LocalDate to a short date string (e.g., "12/31/2025" or "31/12/2025")
     */
    fun formatShortDate(date: LocalDate): String

    /**
     * Format a LocalDate to a medium date string (e.g., "Dec 31, 2025" or "31 ديسمبر 2025")
     */
    fun formatMediumDate(date: LocalDate): String

    /**
     * Format a LocalDate to a long date string (e.g., "December 31, 2025")
     */
    fun formatLongDate(date: LocalDate): String

    /**
     * Format a LocalDateTime to a short date and time string (e.g., "12/31/2025 3:30 PM")
     */
    fun formatShortDateTime(dateTime: LocalDateTime): String

    /**
     * Format a LocalDateTime to a medium date and time string
     */
    fun formatMediumDateTime(dateTime: LocalDateTime): String

    /**
     * Format time only (e.g., "3:30 PM" or "15:30")
     */
    fun formatTime(dateTime: LocalDateTime): String

    /**
     * Format relative date (e.g., "Today", "Tomorrow", "Yesterday", or actual date)
     */
    fun formatRelativeDate(date: LocalDate): String

    /**
     * Format relative time (e.g., "2 hours ago", "in 3 days")
     */
    fun formatRelativeTime(instant: Instant): String
}

/**
 * Common date formatting utilities that don't require platform-specific implementations
 */
object CommonDateFormatter {
    /**
     * Format day of week
     */
    fun formatDayOfWeek(date: LocalDate): String {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
            else -> ""
        }
    }

    /**
     * Format month name
     */
    fun formatMonth(month: Month): String {
        return when (month) {
            Month.JANUARY -> "January"
            Month.FEBRUARY -> "February"
            Month.MARCH -> "March"
            Month.APRIL -> "April"
            Month.MAY -> "May"
            Month.JUNE -> "June"
            Month.JULY -> "July"
            Month.AUGUST -> "August"
            Month.SEPTEMBER -> "September"
            Month.OCTOBER -> "October"
            Month.NOVEMBER -> "November"
            Month.DECEMBER -> "December"
            else -> ""
        }
    }

    /**
     * Format ISO 8601 date (YYYY-MM-DD)
     */
    fun formatIso8601Date(date: LocalDate): String {
        return date.toString()
    }

    /**
     * Format ISO 8601 date time
     */
    fun formatIso8601DateTime(dateTime: LocalDateTime): String {
        return dateTime.toString()
    }

    /**
     * Check if date is today
     */
    fun isToday(date: LocalDate): Boolean {
        return date == Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    /**
     * Check if date is tomorrow
     */
    fun isTomorrow(date: LocalDate): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return date == today.plus(1, DateTimeUnit.DAY)
    }

    /**
     * Check if date is yesterday
     */
    fun isYesterday(date: LocalDate): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return date == today.minus(1, DateTimeUnit.DAY)
    }
}
