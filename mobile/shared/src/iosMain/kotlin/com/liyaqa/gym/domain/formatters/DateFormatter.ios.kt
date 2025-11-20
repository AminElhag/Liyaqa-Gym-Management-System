package com.liyaqa.gym.domain.formatters

import kotlinx.datetime.*
import platform.Foundation.*

/**
 * iOS implementation of DateFormatter using Foundation DateFormatter
 */
actual class DateFormatter {
    actual fun formatShortDate(date: LocalDate): String {
        val formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterShortStyle
        formatter.timeStyle = NSDateFormatterNoStyle
        return formatter.stringFromDate(date.toNSDate())
    }

    actual fun formatMediumDate(date: LocalDate): String {
        val formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterMediumStyle
        formatter.timeStyle = NSDateFormatterNoStyle
        return formatter.stringFromDate(date.toNSDate())
    }

    actual fun formatLongDate(date: LocalDate): String {
        val formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterLongStyle
        formatter.timeStyle = NSDateFormatterNoStyle
        return formatter.stringFromDate(date.toNSDate())
    }

    actual fun formatShortDateTime(dateTime: LocalDateTime): String {
        val formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterShortStyle
        formatter.timeStyle = NSDateFormatterShortStyle
        return formatter.stringFromDate(dateTime.toNSDate())
    }

    actual fun formatMediumDateTime(dateTime: LocalDateTime): String {
        val formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterMediumStyle
        formatter.timeStyle = NSDateFormatterMediumStyle
        return formatter.stringFromDate(dateTime.toNSDate())
    }

    actual fun formatTime(dateTime: LocalDateTime): String {
        val formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterNoStyle
        formatter.timeStyle = NSDateFormatterShortStyle
        return formatter.stringFromDate(dateTime.toNSDate())
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

    private fun LocalDate.toNSDate(): NSDate {
        val components = NSDateComponents()
        components.year = year.toLong()
        components.month = monthNumber.toLong()
        components.day = dayOfMonth.toLong()

        val calendar = NSCalendar.currentCalendar
        return calendar.dateFromComponents(components) ?: NSDate()
    }

    private fun LocalDateTime.toNSDate(): NSDate {
        val components = NSDateComponents()
        components.year = year.toLong()
        components.month = monthNumber.toLong()
        components.day = dayOfMonth.toLong()
        components.hour = hour.toLong()
        components.minute = minute.toLong()
        components.second = second.toLong()

        val calendar = NSCalendar.currentCalendar
        return calendar.dateFromComponents(components) ?: NSDate()
    }
}
