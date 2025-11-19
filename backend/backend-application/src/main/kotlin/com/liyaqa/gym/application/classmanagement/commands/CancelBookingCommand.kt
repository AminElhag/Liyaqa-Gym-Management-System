package com.liyaqa.gym.application.classmanagement.commands

import java.util.UUID

/**
 * Command to cancel a booking.
 *
 * @property bookingId The ID of the booking to cancel
 * @property reason The reason for cancellation
 */
data class CancelBookingCommand(
    val bookingId: UUID,
    val reason: String
) {
    init {
        require(reason.isNotBlank()) {
            "Cancellation reason is required and cannot be blank"
        }
    }
}
