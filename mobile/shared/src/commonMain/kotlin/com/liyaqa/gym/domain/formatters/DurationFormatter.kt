package com.liyaqa.gym.domain.formatters

import kotlinx.datetime.*
import kotlin.math.abs
import kotlin.time.Duration

/**
 * Platform-specific duration formatter interface
 * Implementations should use platform-native duration formatting when beneficial
 */
expect class DurationFormatter() {
    /**
     * Format duration in a human-readable format
     * e.g., "1 hour 30 minutes", "45 minutes"
     */
    fun formatDuration(duration: Duration): String

    /**
     * Format duration relative to now
     * e.g., "2 hours ago", "in 3 days"
     */
    fun formatRelativeDuration(duration: Duration, isPast: Boolean): String
}

/**
 * Common duration formatting utilities
 */
object CommonDurationFormatter {
    /**
     * Format duration in hours and minutes
     * e.g., "1h 30m", "45m"
     */
    fun formatShort(duration: Duration): String {
        val totalMinutes = duration.inWholeMinutes
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    /**
     * Format duration in long format
     * e.g., "1 hour 30 minutes", "45 minutes"
     */
    fun formatLong(duration: Duration): String {
        val totalMinutes = duration.inWholeMinutes
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "$hours ${pluralize(hours.toInt(), "hour")} $minutes ${pluralize(minutes.toInt(), "minute")}"
            hours > 0 -> "$hours ${pluralize(hours.toInt(), "hour")}"
            else -> "$minutes ${pluralize(minutes.toInt(), "minute")}"
        }
    }

    /**
     * Format time difference between two LocalDateTime instances
     */
    fun formatTimeBetween(start: LocalDateTime, end: LocalDateTime): String {
        val startInstant = start.toInstant(TimeZone.currentSystemDefault())
        val endInstant = end.toInstant(TimeZone.currentSystemDefault())
        val duration = endInstant - startInstant
        return formatLong(duration)
    }

    /**
     * Format relative time from now
     * e.g., "2 hours ago", "in 3 days"
     */
    fun formatRelativeToNow(instant: Instant): String {
        val now = Clock.System.now()
        val duration = instant - now
        val isPast = duration.isNegative()
        val absDuration = duration.absoluteValue

        return when {
            absDuration.inWholeMinutes < 1 -> "just now"
            absDuration.inWholeMinutes < 60 -> {
                val minutes = absDuration.inWholeMinutes
                if (isPast) "$minutes ${pluralize(minutes.toInt(), "minute")} ago"
                else "in $minutes ${pluralize(minutes.toInt(), "minute")}"
            }
            absDuration.inWholeHours < 24 -> {
                val hours = absDuration.inWholeHours
                if (isPast) "$hours ${pluralize(hours.toInt(), "hour")} ago"
                else "in $hours ${pluralize(hours.toInt(), "hour")}"
            }
            absDuration.inWholeDays < 7 -> {
                val days = absDuration.inWholeDays
                if (isPast) "$days ${pluralize(days.toInt(), "day")} ago"
                else "in $days ${pluralize(days.toInt(), "day")}"
            }
            absDuration.inWholeDays < 30 -> {
                val weeks = absDuration.inWholeDays / 7
                if (isPast) "$weeks ${pluralize(weeks.toInt(), "week")} ago"
                else "in $weeks ${pluralize(weeks.toInt(), "week")}"
            }
            absDuration.inWholeDays < 365 -> {
                val months = absDuration.inWholeDays / 30
                if (isPast) "$months ${pluralize(months.toInt(), "month")} ago"
                else "in $months ${pluralize(months.toInt(), "month")}"
            }
            else -> {
                val years = absDuration.inWholeDays / 365
                if (isPast) "$years ${pluralize(years.toInt(), "year")} ago"
                else "in $years ${pluralize(years.toInt(), "year")}"
            }
        }
    }

    /**
     * Format class duration (start to end time)
     */
    fun formatClassDuration(startTime: LocalDateTime, endTime: LocalDateTime): String {
        val duration = endTime.toInstant(TimeZone.currentSystemDefault()) -
                      startTime.toInstant(TimeZone.currentSystemDefault())
        return formatShort(duration)
    }

    /**
     * Format countdown to a specific date time
     * e.g., "2 hours 30 minutes", "3 days"
     */
    fun formatCountdown(targetDateTime: LocalDateTime): String {
        val now = Clock.System.now()
        val target = targetDateTime.toInstant(TimeZone.currentSystemDefault())
        val duration = target - now

        if (duration.isNegative()) {
            return "Started"
        }

        val totalMinutes = duration.inWholeMinutes
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60

        return when {
            days > 0 -> "$days ${pluralize(days.toInt(), "day")}"
            hours > 0 -> "$hours ${pluralize(hours.toInt(), "hour")} $minutes ${pluralize(minutes.toInt(), "minute")}"
            else -> "$minutes ${pluralize(minutes.toInt(), "minute")}"
        }
    }

    /**
     * Pluralize word based on count
     */
    private fun pluralize(count: Int, word: String): String {
        return if (count == 1) word else "${word}s"
    }
}
