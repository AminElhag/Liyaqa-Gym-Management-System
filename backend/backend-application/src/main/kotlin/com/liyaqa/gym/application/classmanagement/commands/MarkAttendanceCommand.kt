package com.liyaqa.gym.application.classmanagement.commands

import java.util.UUID

/**
 * Command to mark class attendance.
 *
 * @property bookingId The ID of the booking to mark attendance for
 * @property attended Whether the member attended or was a no-show
 * @property markedByUserId The ID of the user (trainer/staff) marking attendance
 */
data class MarkAttendanceCommand(
    val bookingId: UUID,
    val attended: Boolean,
    val markedByUserId: UUID
)
