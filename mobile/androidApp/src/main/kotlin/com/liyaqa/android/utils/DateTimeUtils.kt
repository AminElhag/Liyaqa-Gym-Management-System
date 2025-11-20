package com.liyaqa.android.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utility object for date and time operations
 */
object DateTimeUtils {

    private const val DATE_FORMAT = "dd/MM/yyyy"
    private const val TIME_FORMAT = "HH:mm"
    private const val DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm"
    private const val ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    /**
     * Format date to display format
     */
    fun formatDate(date: Date, locale: Locale = Locale.getDefault()): String {
        val format = SimpleDateFormat(DATE_FORMAT, locale)
        return format.format(date)
    }

    /**
     * Format time to display format
     */
    fun formatTime(date: Date, locale: Locale = Locale.getDefault()): String {
        val format = SimpleDateFormat(TIME_FORMAT, locale)
        return format.format(date)
    }

    /**
     * Format date and time to display format
     */
    fun formatDateTime(date: Date, locale: Locale = Locale.getDefault()): String {
        val format = SimpleDateFormat(DATE_TIME_FORMAT, locale)
        return format.format(date)
    }

    /**
     * Format date to ISO 8601 format
     */
    fun formatIsoDateTime(date: Date): String {
        val format = SimpleDateFormat(ISO_FORMAT, Locale.US)
        return format.format(date)
    }

    /**
     * Parse ISO 8601 date string
     */
    fun parseIsoDateTime(dateString: String): Date? {
        return try {
            val format = SimpleDateFormat(ISO_FORMAT, Locale.US)
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get relative time string (e.g., "2 hours ago", "in 3 days")
     */
    fun getRelativeTimeString(date: Date): String {
        val now = Date()
        val diff = date.time - now.time

        return when {
            diff < 0 -> {
                // Past
                val absDiff = -diff
                when {
                    absDiff < TimeUnit.MINUTES.toMillis(1) -> "just now"
                    absDiff < TimeUnit.HOURS.toMillis(1) -> {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(absDiff)
                        "$minutes minute${if (minutes > 1) "s" else ""} ago"
                    }
                    absDiff < TimeUnit.DAYS.toMillis(1) -> {
                        val hours = TimeUnit.MILLISECONDS.toHours(absDiff)
                        "$hours hour${if (hours > 1) "s" else ""} ago"
                    }
                    absDiff < TimeUnit.DAYS.toMillis(7) -> {
                        val days = TimeUnit.MILLISECONDS.toDays(absDiff)
                        "$days day${if (days > 1) "s" else ""} ago"
                    }
                    else -> formatDate(date)
                }
            }
            else -> {
                // Future
                when {
                    diff < TimeUnit.MINUTES.toMillis(1) -> "in a moment"
                    diff < TimeUnit.HOURS.toMillis(1) -> {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                        "in $minutes minute${if (minutes > 1) "s" else ""}"
                    }
                    diff < TimeUnit.DAYS.toMillis(1) -> {
                        val hours = TimeUnit.MILLISECONDS.toHours(diff)
                        "in $hours hour${if (hours > 1) "s" else ""}"
                    }
                    diff < TimeUnit.DAYS.toMillis(7) -> {
                        val days = TimeUnit.MILLISECONDS.toDays(diff)
                        "in $days day${if (days > 1) "s" else ""}"
                    }
                    else -> formatDate(date)
                }
            }
        }
    }

    /**
     * Check if date is today
     */
    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply { time = date }

        return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Check if date is tomorrow
     */
    fun isTomorrow(date: Date): Boolean {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val dateCalendar = Calendar.getInstance().apply { time = date }

        return tomorrow.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    }
}
