package com.liyaqa.gym.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Domain model representing a gym class/session.
 */
data class GymClass(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val classType: ClassType,
    val trainerId: UUID,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val maxCapacity: Int,
    val currentBookings: Int = 0,
    val status: ClassStatus
) {
    init {
        require(name.isNotBlank()) { "Class name cannot be blank" }
        require(maxCapacity > 0) { "Max capacity must be greater than 0" }
        require(currentBookings >= 0) { "Current bookings cannot be negative" }
        require(currentBookings <= maxCapacity) { "Current bookings cannot exceed max capacity" }
        require(endTime.isAfter(startTime)) { "End time must be after start time" }
    }

    fun isFull(): Boolean = currentBookings >= maxCapacity

    fun hasAvailableSpots(): Boolean = currentBookings < maxCapacity

    fun availableSpots(): Int = maxCapacity - currentBookings
}

enum class ClassType {
    YOGA,
    PILATES,
    CARDIO,
    STRENGTH_TRAINING,
    HIIT,
    SPINNING,
    ZUMBA,
    CROSSFIT,
    BOXING,
    SWIMMING
}

enum class ClassStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
