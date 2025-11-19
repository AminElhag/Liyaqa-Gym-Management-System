package com.liyaqa.gym.application.classmanagement.commands

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

/**
 * Command to create a new class schedule.
 *
 * @property classId The ID of the class to schedule
 * @property trainerId The ID of the trainer assigned to the class
 * @property roomId The ID of the room where the class will be held (optional)
 * @property startTime The start time of the class
 * @property endTime The end time of the class
 * @property dayOfWeek The day of week for the class
 */
data class CreateScheduleCommand(
    val classId: UUID,
    val trainerId: UUID,
    val roomId: UUID?,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val dayOfWeek: DayOfWeek
) {
    init {
        require(endTime.isAfter(startTime)) {
            "End time must be after start time"
        }
    }
}
