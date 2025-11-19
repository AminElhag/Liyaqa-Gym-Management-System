package com.liyaqa.gym.application.classmanagement.dto

import com.liyaqa.gym.domain.entities.BookingStatus
import java.time.Instant
import java.util.UUID

/**
 * Data Transfer Object for Booking entity.
 *
 * @property id The unique identifier of the booking
 * @property memberId The ID of the member who made the booking
 * @property scheduleId The ID of the class schedule
 * @property status The status of the booking
 * @property bookedAt The timestamp when the booking was made
 * @property waitlistPosition The position in the waitlist (if applicable)
 * @property confirmedAt The timestamp when the booking was confirmed
 * @property checkedInAt The timestamp when the member checked in
 * @property cancelledAt The timestamp when the booking was cancelled
 * @property cancellationReason The reason for cancellation
 * @property noShowMarkedAt The timestamp when marked as no-show
 * @property createdAt The timestamp when the booking was created
 * @property updatedAt The timestamp when the booking was last updated
 */
data class BookingDTO(
    val id: UUID,
    val memberId: UUID,
    val scheduleId: UUID,
    val status: BookingStatus,
    val bookedAt: Instant,
    val waitlistPosition: Int?,
    val confirmedAt: Instant?,
    val checkedInAt: Instant?,
    val cancelledAt: Instant?,
    val cancellationReason: String?,
    val noShowMarkedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
