package com.liyaqa.gym.domain.formatters

import kotlin.time.Duration

/**
 * iOS implementation of DurationFormatter
 */
actual class DurationFormatter {
    actual fun formatDuration(duration: Duration): String {
        return CommonDurationFormatter.formatLong(duration)
    }

    actual fun formatRelativeDuration(duration: Duration, isPast: Boolean): String {
        val absDuration = duration.absoluteValue

        return when {
            absDuration.inWholeMinutes < 1 -> "just now"
            absDuration.inWholeMinutes < 60 -> {
                val minutes = absDuration.inWholeMinutes
                if (isPast) "$minutes min ago" else "in $minutes min"
            }
            absDuration.inWholeHours < 24 -> {
                val hours = absDuration.inWholeHours
                if (isPast) "$hours hr ago" else "in $hours hr"
            }
            absDuration.inWholeDays < 7 -> {
                val days = absDuration.inWholeDays
                if (isPast) "$days day${if (days > 1) "s" else ""} ago"
                else "in $days day${if (days > 1) "s" else ""}"
            }
            else -> {
                val weeks = absDuration.inWholeDays / 7
                if (isPast) "$weeks week${if (weeks > 1) "s" else ""} ago"
                else "in $weeks week${if (weeks > 1) "s" else ""}"
            }
        }
    }
}
