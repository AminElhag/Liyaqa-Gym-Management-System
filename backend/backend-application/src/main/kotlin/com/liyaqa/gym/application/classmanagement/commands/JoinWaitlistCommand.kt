package com.liyaqa.gym.application.classmanagement.commands

import java.util.UUID

/**
 * Command to join a class waitlist.
 *
 * @property memberId The ID of the member joining the waitlist
 * @property scheduleId The ID of the class schedule
 */
data class JoinWaitlistCommand(
    val memberId: UUID,
    val scheduleId: UUID
)
