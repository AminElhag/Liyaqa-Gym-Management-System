package com.liyaqa.gym.application.classmanagement.commands

import java.util.UUID

/**
 * Command to book a class for a member.
 *
 * @property memberId The ID of the member booking the class
 * @property scheduleId The ID of the class schedule to book
 */
data class BookClassCommand(
    val memberId: UUID,
    val scheduleId: UUID
)
