package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.util.UUID

/**
 * Zone entity representing restricted areas within a branch.
 * Examples: Cardio Zone, Weight Training Zone, Pool, Spa, VIP Area.
 */
data class Zone(
    val id: UUID,
    val branchId: UUID,
    val name: String,
    val description: String?,
    val requiredFeatures: List<String>, // Features required in membership plan
    val capacity: Int?,
    val genderRestriction: Gender?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Zone name cannot be blank" }
        capacity?.let {
            require(it > 0) { "Zone capacity must be positive" }
        }
    }

    fun activate(): Zone {
        return copy(isActive = true, updatedAt = Instant.now())
    }

    fun deactivate(): Zone {
        return copy(isActive = false, updatedAt = Instant.now())
    }

    fun canAccess(memberGender: Gender, planFeatures: List<String>): Boolean {
        // Check gender restriction
        if (genderRestriction != null && genderRestriction != memberGender) {
            return false
        }

        // Check if member's plan includes all required features
        return requiredFeatures.all { required -> planFeatures.contains(required) }
    }

    companion object {
        fun create(
            branchId: UUID,
            name: String,
            description: String?,
            requiredFeatures: List<String> = emptyList(),
            capacity: Int? = null,
            genderRestriction: Gender? = null
        ): Zone {
            val now = Instant.now()
            return Zone(
                id = UUID.randomUUID(),
                branchId = branchId,
                name = name,
                description = description,
                requiredFeatures = requiredFeatures,
                capacity = capacity,
                genderRestriction = genderRestriction,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
