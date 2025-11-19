package com.liyaqa.gym.application.classmanagement.dto

import java.time.Instant
import java.util.UUID

/**
 * DTO representing a confirmation of a booking cancellation.
 *
 * @property bookingId The ID of the cancelled booking
 * @property refundIssued Whether a refund was issued
 * @property creditRefunded Whether class credit was refunded
 * @property lateCancellationFee The fee charged for late cancellation (if any)
 * @property cancelledAt The timestamp of cancellation
 * @property message A confirmation message
 */
data class CancellationConfirmation(
    val bookingId: UUID,
    val refundIssued: Boolean,
    val creditRefunded: Boolean,
    val lateCancellationFee: Double?,
    val cancelledAt: Instant,
    val message: String
)
