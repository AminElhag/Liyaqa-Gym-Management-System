package com.liyaqa.gym.domain.valueobjects

import java.time.Duration
import java.time.LocalTime

/**
 * Value object representing a time slot with start and end times.
 * Validates that end time is after start time.
 */
data class TimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    init {
        require(endTime.isAfter(startTime)) {
            "End time must be after start time. Got start: $startTime, end: $endTime"
        }
    }

    val duration: Duration
        get() = Duration.between(startTime, endTime)

    val durationMinutes: Long
        get() = duration.toMinutes()

    fun overlaps(other: TimeSlot): Boolean {
        return startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime)
    }

    fun contains(time: LocalTime): Boolean {
        return !time.isBefore(startTime) && time.isBefore(endTime)
    }

    fun isBefore(other: TimeSlot): Boolean {
        return endTime.isBefore(other.startTime) || endTime == other.startTime
    }

    fun isAfter(other: TimeSlot): Boolean {
        return startTime.isAfter(other.endTime) || startTime == other.endTime
    }

    companion object {
        fun of(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): TimeSlot {
            return TimeSlot(
                LocalTime.of(startHour, startMinute),
                LocalTime.of(endHour, endMinute)
            )
        }

        fun fromDuration(startTime: LocalTime, durationMinutes: Long): TimeSlot {
            require(durationMinutes > 0) { "Duration must be positive" }
            return TimeSlot(startTime, startTime.plusMinutes(durationMinutes))
        }

        fun fromStartAndEnd(start: String, end: String): TimeSlot {
            return TimeSlot(LocalTime.parse(start), LocalTime.parse(end))
        }
    }
}
