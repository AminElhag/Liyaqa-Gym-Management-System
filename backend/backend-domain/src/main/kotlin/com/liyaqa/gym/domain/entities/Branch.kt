package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.Address
import java.time.Instant
import java.util.UUID

/**
 * Branch entity representing a physical gym location.
 * Belongs to an Organization.
 */
data class Branch(
    val id: UUID,
    val organizationId: UUID,
    val name: String,
    val address: Address,
    val facilityType: FacilityType,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Branch name cannot be blank" }
    }

    fun activate(): Branch {
        return copy(isActive = true, updatedAt = Instant.now())
    }

    fun deactivate(): Branch {
        return copy(isActive = false, updatedAt = Instant.now())
    }

    fun updateAddress(newAddress: Address): Branch {
        return copy(address = newAddress, updatedAt = Instant.now())
    }

    fun canAcceptGender(gender: Gender): Boolean {
        return when (facilityType) {
            FacilityType.MALE_ONLY -> gender == Gender.MALE
            FacilityType.FEMALE_ONLY -> gender == Gender.FEMALE
            FacilityType.FAMILY -> true
        }
    }

    companion object {
        fun create(
            organizationId: UUID,
            name: String,
            address: Address,
            facilityType: FacilityType
        ): Branch {
            val now = Instant.now()
            return Branch(
                id = UUID.randomUUID(),
                organizationId = organizationId,
                name = name,
                address = address,
                facilityType = facilityType,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Facility type indicating gender access restrictions
 */
enum class FacilityType {
    MALE_ONLY,
    FEMALE_ONLY,
    FAMILY
}

/**
 * Gender enumeration
 */
enum class Gender {
    MALE,
    FEMALE
}
