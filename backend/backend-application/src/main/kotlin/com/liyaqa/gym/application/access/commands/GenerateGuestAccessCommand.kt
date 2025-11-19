package com.liyaqa.gym.application.access.commands

import java.util.UUID

/**
 * Command for generating temporary guest access.
 *
 * @property hostMemberId The unique identifier of the host member
 * @property branchId The unique identifier of the branch
 * @property guestName The name of the guest
 * @property guestPhone The phone number of the guest (optional)
 * @property validityHours Number of hours the guest access is valid (default: 24)
 */
data class GenerateGuestAccessCommand(
    val hostMemberId: UUID,
    val branchId: UUID,
    val guestName: String,
    val guestPhone: String? = null,
    val validityHours: Long = 24
) {
    init {
        require(guestName.isNotBlank()) {
            "Guest name cannot be blank"
        }
        require(validityHours > 0 && validityHours <= 168) {
            "Validity hours must be between 1 and 168 (7 days)"
        }
    }
}
