package com.liyaqa.gym.application.access.dto

import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * DTO representing check-out confirmation.
 *
 * @property accessLogId The unique identifier of the access log
 * @property memberId The unique identifier of the member
 * @property branchId The unique identifier of the branch
 * @property checkInTime The time when member checked in
 * @property checkOutTime The time when member checked out
 * @property duration The duration of the visit
 * @property message Confirmation message
 */
data class CheckOutConfirmation(
    val accessLogId: UUID,
    val memberId: UUID,
    val branchId: UUID,
    val checkInTime: Instant,
    val checkOutTime: Instant,
    val duration: Duration,
    val message: String = "Checked out successfully"
) {
    fun getDurationMinutes(): Long = duration.toMinutes()

    fun getDurationHours(): Long = duration.toHours()
}
