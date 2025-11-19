package com.liyaqa.gym.application.classmanagement.dto

import com.liyaqa.gym.domain.entities.ScheduleStatus
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Data Transfer Object for ClassSchedule entity.
 *
 * @property id The unique identifier of the schedule
 * @property classId The ID of the class
 * @property trainerId The ID of the trainer
 * @property roomId The ID of the room (optional)
 * @property dayOfWeek The day of week for the schedule
 * @property startTime The start time of the class
 * @property endTime The end time of the class
 * @property startDate The start date/time
 * @property endDate The end date/time (optional)
 * @property currentBookings The current number of bookings
 * @property waitingList The number of people on the waiting list
 * @property status The status of the schedule
 * @property createdAt The timestamp when the schedule was created
 * @property updatedAt The timestamp when the schedule was last updated
 */
data class ScheduleDTO(
    val id: UUID,
    val classId: UUID,
    val trainerId: UUID,
    val roomId: UUID?,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val currentBookings: Int,
    val waitingList: Int,
    val status: ScheduleStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)
