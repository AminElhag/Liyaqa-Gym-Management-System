package com.liyaqa.gym.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Booking entity representing a member's reservation for a class schedule.
 * Links Member to ClassSchedule.
 */
@Serializable
data class Booking(
    val id: String,
    val memberId: String,
    val scheduleId: String,
    val status: BookingStatus,
    val bookedAt: Instant,
    val waitlistPosition: Int? = null,
    val confirmedAt: Instant? = null,
    val checkedInAt: Instant? = null,
    val cancelledAt: Instant? = null,
    val cancellationReason: String? = null,
    val noShowMarkedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun isConfirmed(): Boolean = status == BookingStatus.CONFIRMED

    fun isWaitlisted(): Boolean = status == BookingStatus.WAITLISTED

    fun isCancelled(): Boolean = status == BookingStatus.CANCELLED

    fun isNoShow(): Boolean = status == BookingStatus.NO_SHOW

    fun isAttended(): Boolean = status == BookingStatus.ATTENDED

    fun canCancel(): Boolean {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.WAITLISTED
    }

    fun canCheckIn(): Boolean = status == BookingStatus.CONFIRMED

    fun statusDisplayText(): String {
        return when (status) {
            BookingStatus.CONFIRMED -> "Confirmed"
            BookingStatus.WAITLISTED -> waitlistPosition?.let { "Waitlisted (#$it)" } ?: "Waitlisted"
            BookingStatus.ATTENDED -> "Attended"
            BookingStatus.CANCELLED -> "Cancelled"
            BookingStatus.NO_SHOW -> "No Show"
        }
    }
}
