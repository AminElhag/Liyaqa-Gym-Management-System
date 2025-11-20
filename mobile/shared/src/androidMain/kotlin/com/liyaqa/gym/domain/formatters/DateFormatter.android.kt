package com.liyaqa.gym.domain.formatters

import kotlinx.datetime.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Android implementation of DateFormatter using Java DateFormat
 */
actual class DateFormatter {
    private val locale = Locale.getDefault()

    actual fun formatShortDate(date: LocalDate): String {
        val format = SimpleDateFormat("MM/dd/yyyy", locale)
        return format.format(date.toJavaLocalDate())
    }

    actual fun formatMediumDate(date: LocalDate): String {
        val format = SimpleDateFormat("MMM dd, yyyy", locale)
        return format.format(date.toJavaLocalDate())
    }

    actual fun formatLongDate(date: LocalDate): String {
        val format = SimpleDateFormat("MMMM dd, yyyy", locale)
        return format.format(date.toJavaLocalDate())
    }

    actual fun formatShortDateTime(dateTime: LocalDateTime): String {
        val format = SimpleDateFormat("MM/dd/yyyy hh:mm a", locale)
        return format.format(dateTime.toJavaLocalDateTime())
    }

    actual fun formatMediumDateTime(dateTime: LocalDateTime): String {
        val format = SimpleDateFormat("MMM dd, yyyy hh:mm a", locale)
        return format.format(dateTime.toJavaLocalDateTime())
    }

    actual fun formatTime(dateTime: LocalDateTime): String {
        val format = SimpleDateFormat("hh:mm a", locale)
        return format.format(dateTime.toJavaLocalDateTime())
    }

    actual fun formatRelativeDate(date: LocalDate): String {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return when {
            date == today -> "Today"
            date == today.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
            date == today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
            else -> formatMediumDate(date)
        }
    }

    actual fun formatRelativeTime(instant: Instant): String {
        return CommonDurationFormatter.formatRelativeToNow(instant)
    }

    private fun LocalDate.toJavaLocalDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, monthNumber - 1, dayOfMonth, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun LocalDateTime.toJavaLocalDateTime(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, monthNumber - 1, dayOfMonth, hour, minute, second)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
