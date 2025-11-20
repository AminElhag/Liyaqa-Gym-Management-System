package com.liyaqa.gym.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * ClassSchedule entity representing a scheduled instance of a class.
 */
@Serializable
data class ClassSchedule(
    val id: String,
    val classId: String,
    val instructorId: String? = null,
    val instructorName: String? = null,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val capacity: Int,
    val bookedCount: Int,
    val waitlistCount: Int,
    val isCancelled: Boolean,
    val cancellationReason: String? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun availableSpots(): Int = capacity - bookedCount

    fun isFull(): Boolean = bookedCount >= capacity

    fun hasWaitlist(): Boolean = waitlistCount > 0

    fun canBook(): Boolean = !isCancelled && !isFull()

    fun capacityPercentage(): Int {
        return if (capacity > 0) {
            ((bookedCount.toFloat() / capacity) * 100).toInt()
        } else {
            0
        }
    }

    fun statusText(): String {
        return when {
            isCancelled -> "Cancelled"
            isFull() -> "Full"
            availableSpots() <= 3 -> "Almost Full (${availableSpots()} spots)"
            else -> "${availableSpots()} spots available"
        }
    }
}
