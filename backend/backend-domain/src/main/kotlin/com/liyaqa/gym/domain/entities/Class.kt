package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.util.UUID

/**
 * Class entity representing a fitness class offering.
 * Belongs to a Branch.
 */
data class Class(
    val id: UUID,
    val branchId: UUID,
    val name: String,
    val nameArabic: String?,
    val description: String?,
    val type: ClassType,
    val level: ClassLevel,
    val capacity: Int,
    val durationMinutes: Int,
    val genderRestriction: Gender?,
    val imageUrl: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Class name cannot be blank" }
        require(capacity > 0) { "Capacity must be positive" }
        require(durationMinutes > 0) { "Duration must be positive" }
    }

    fun activate(): Class {
        return copy(isActive = true, updatedAt = Instant.now())
    }

    fun deactivate(): Class {
        return copy(isActive = false, updatedAt = Instant.now())
    }

    fun updateCapacity(newCapacity: Int): Class {
        require(newCapacity > 0) { "Capacity must be positive" }
        return copy(capacity = newCapacity, updatedAt = Instant.now())
    }

    fun canMemberJoin(memberGender: Gender): Boolean {
        return genderRestriction == null || genderRestriction == memberGender
    }

    companion object {
        fun create(
            branchId: UUID,
            name: String,
            nameArabic: String?,
            type: ClassType,
            level: ClassLevel,
            capacity: Int,
            durationMinutes: Int,
            genderRestriction: Gender? = null
        ): Class {
            val now = Instant.now()
            return Class(
                id = UUID.randomUUID(),
                branchId = branchId,
                name = name,
                nameArabic = nameArabic,
                description = null,
                type = type,
                level = level,
                capacity = capacity,
                durationMinutes = durationMinutes,
                genderRestriction = genderRestriction,
                imageUrl = null,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Class type enumeration
 */
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
    SWIMMING,
    FUNCTIONAL_TRAINING,
    STRETCHING,
    MARTIAL_ARTS,
    DANCE,
    OTHER
}

/**
 * Class difficulty level
 */
enum class ClassLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ALL_LEVELS
}
