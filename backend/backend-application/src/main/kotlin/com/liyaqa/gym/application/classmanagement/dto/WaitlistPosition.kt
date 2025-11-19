package com.liyaqa.gym.application.classmanagement.dto

import java.util.UUID

/**
 * DTO representing a member's position in a waitlist.
 *
 * @property bookingId The ID of the waitlisted booking
 * @property position The position in the waitlist
 * @property estimatedWaitTime An estimate of how long before a spot opens (optional)
 */
data class WaitlistPosition(
    val bookingId: UUID,
    val position: Int,
    val estimatedWaitTime: String?
)
