package com.liyaqa.gym.presentation.dto.classmanagement

import jakarta.validation.constraints.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request to book a class
 */
data class BookClassRequest(
    @field:NotNull(message = "Schedule ID is required")
    val scheduleId: UUID,

    val memberId: UUID? = null // Will be extracted from security context if not provided
)

/**
 * Request to cancel a booking
 */
data class CancelBookingRequest(
    @field:Size(max = 500, message = "Reason cannot exceed 500 characters")
    val reason: String?
)

/**
 * Request to check in for a booking
 */
data class CheckInRequest(
    val notes: String?
)

/**
 * Request to join waitlist
 */
data class JoinWaitlistRequest(
    @field:NotNull(message = "Schedule ID is required")
    val scheduleId: UUID,

    val memberId: UUID? = null // Will be extracted from security context if not provided
)

/**
 * Request to bulk book for a group
 */
data class BulkBookRequest(
    @field:NotNull(message = "Schedule ID is required")
    val scheduleId: UUID,

    @field:NotEmpty(message = "Member IDs list cannot be empty")
    @field:Size(min = 1, max = 50, message = "Can book for 1 to 50 members at once")
    val memberIds: List<UUID>,

    val notes: String?
)

/**
 * Response containing booking details
 */
data class BookingResponse(
    val id: UUID,
    val memberId: UUID,
    val memberName: String?,
    val scheduleId: UUID,
    val className: String?,
    val classTime: LocalDateTime?,
    val status: String,
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

/**
 * Summary response for booking listings
 */
data class BookingSummaryResponse(
    val id: UUID,
    val memberId: UUID,
    val memberName: String?,
    val scheduleId: UUID,
    val className: String?,
    val classTime: LocalDateTime?,
    val status: String,
    val bookedAt: Instant
)

/**
 * Response for check-in operation
 */
data class CheckInResponse(
    val bookingId: UUID,
    val memberId: UUID,
    val scheduleId: UUID,
    val checkedInAt: Instant,
    val message: String
)

/**
 * Response for waitlist operation
 */
data class WaitlistResponse(
    val bookingId: UUID,
    val memberId: UUID,
    val scheduleId: UUID,
    val waitlistPosition: Int,
    val message: String
)

/**
 * Response for bulk booking operation
 */
data class BulkBookingResponse(
    val scheduleId: UUID,
    val successfulBookings: List<BookingSummaryResponse>,
    val failedBookings: List<FailedBooking>,
    val totalRequested: Int,
    val totalSuccess: Int,
    val totalFailed: Int
)

/**
 * Details of a failed booking in bulk operation
 */
data class FailedBooking(
    val memberId: UUID,
    val reason: String
)

/**
 * Response for cancellation confirmation
 */
data class CancellationResponse(
    val bookingId: UUID,
    val cancelledAt: Instant,
    val reason: String?,
    val refundAmount: Double?,
    val message: String
)
