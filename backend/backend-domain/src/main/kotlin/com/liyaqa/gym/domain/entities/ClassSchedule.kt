package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.TimeSlot
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

/**
 * ClassSchedule entity representing a scheduled instance of a class.
 * Links Class, Trainer, Room, and time information.
 */
data class ClassSchedule(
    val id: UUID,
    val classId: UUID,
    val trainerId: UUID,
    val roomId: UUID?,
    val timeSlot: TimeSlot,
    val dayOfWeek: DayOfWeek,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val currentBookings: Int,
    val waitingList: Int,
    val status: ScheduleStatus,
    val recurrencePattern: RecurrencePattern?,
    val cancellationReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(currentBookings >= 0) { "Current bookings cannot be negative" }
        require(waitingList >= 0) { "Waiting list cannot be negative" }
        endDate?.let {
            require(!it.isBefore(startDate)) {
                "End date cannot be before start date"
            }
        }
    }

    fun hasAvailableSpots(maxCapacity: Int): Boolean {
        return currentBookings < maxCapacity
    }

    fun isFull(maxCapacity: Int): Boolean {
        return currentBookings >= maxCapacity
    }

    fun availableSpots(maxCapacity: Int): Int {
        return maxCapacity - currentBookings
    }

    fun incrementBooking(): ClassSchedule {
        return copy(currentBookings = currentBookings + 1, updatedAt = Instant.now())
    }

    fun decrementBooking(): ClassSchedule {
        require(currentBookings > 0) { "Cannot decrement bookings below zero" }
        return copy(currentBookings = currentBookings - 1, updatedAt = Instant.now())
    }

    fun addToWaitingList(): ClassSchedule {
        return copy(waitingList = waitingList + 1, updatedAt = Instant.now())
    }

    fun removeFromWaitingList(): ClassSchedule {
        require(waitingList > 0) { "Cannot decrement waiting list below zero" }
        return copy(waitingList = waitingList - 1, updatedAt = Instant.now())
    }

    fun cancel(reason: String): ClassSchedule {
        return copy(
            status = ScheduleStatus.CANCELLED,
            cancellationReason = reason,
            updatedAt = Instant.now()
        )
    }

    fun complete(): ClassSchedule {
        return copy(status = ScheduleStatus.COMPLETED, updatedAt = Instant.now())
    }

    fun isActive(): Boolean = status == ScheduleStatus.SCHEDULED

    companion object {
        fun create(
            classId: UUID,
            trainerId: UUID,
            roomId: UUID?,
            timeSlot: TimeSlot,
            dayOfWeek: DayOfWeek,
            startDate: LocalDateTime,
            endDate: LocalDateTime? = null,
            recurrencePattern: RecurrencePattern? = null
        ): ClassSchedule {
            val now = Instant.now()
            return ClassSchedule(
                id = UUID.randomUUID(),
                classId = classId,
                trainerId = trainerId,
                roomId = roomId,
                timeSlot = timeSlot,
                dayOfWeek = dayOfWeek,
                startDate = startDate,
                endDate = endDate,
                currentBookings = 0,
                waitingList = 0,
                status = ScheduleStatus.SCHEDULED,
                recurrencePattern = recurrencePattern,
                cancellationReason = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Schedule status enumeration
 */
enum class ScheduleStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

/**
 * Recurrence pattern for recurring schedules
 */
enum class RecurrencePattern {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY
}
