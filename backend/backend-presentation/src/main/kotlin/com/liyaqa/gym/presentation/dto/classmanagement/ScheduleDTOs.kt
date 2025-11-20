package com.liyaqa.gym.presentation.dto.classmanagement

import jakarta.validation.constraints.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Request to create a new class schedule
 */
data class CreateScheduleRequest(
    @field:NotNull(message = "Class ID is required")
    val classId: UUID,

    @field:NotNull(message = "Trainer ID is required")
    val trainerId: UUID,

    val roomId: UUID?,

    @field:NotNull(message = "Start time is required")
    val startTime: LocalTime,

    @field:NotNull(message = "End time is required")
    val endTime: LocalTime,

    @field:NotNull(message = "Day of week is required")
    val dayOfWeek: String,

    @field:NotNull(message = "Start date is required")
    val startDate: LocalDateTime,

    val endDate: LocalDateTime?,

    val recurrencePattern: String?
)

/**
 * Request to update a class schedule
 */
data class UpdateScheduleRequest(
    val trainerId: UUID?,
    val roomId: UUID?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?
)

/**
 * Request to cancel a schedule
 */
data class CancelScheduleRequest(
    @field:NotBlank(message = "Cancellation reason is required")
    @field:Size(max = 500, message = "Reason cannot exceed 500 characters")
    val reason: String,

    @field:NotNull(message = "Notify members flag is required")
    val notifyMembers: Boolean = true
)

/**
 * Response containing schedule details
 */
data class ScheduleResponse(
    val id: UUID,
    val classId: UUID,
    val className: String?,
    val trainerId: UUID,
    val trainerName: String?,
    val roomId: UUID?,
    val roomName: String?,
    val dayOfWeek: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val currentBookings: Int,
    val waitingList: Int,
    val capacity: Int?,
    val availableSpots: Int?,
    val status: String,
    val recurrencePattern: String?,
    val cancellationReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Summary response for schedule listings
 */
data class ScheduleSummaryResponse(
    val id: UUID,
    val classId: UUID,
    val className: String?,
    val trainerId: UUID,
    val trainerName: String?,
    val dayOfWeek: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startDate: LocalDateTime,
    val currentBookings: Int,
    val capacity: Int?,
    val availableSpots: Int?,
    val status: String
)

/**
 * Response for calendar view
 */
data class CalendarResponse(
    val date: LocalDate,
    val schedules: List<ScheduleSummaryResponse>
)

/**
 * Response for availability details
 */
data class AvailabilityResponse(
    val scheduleId: UUID,
    val capacity: Int,
    val currentBookings: Int,
    val availableSpots: Int,
    val waitingList: Int,
    val isFull: Boolean,
    val hasWaitingList: Boolean
)
