package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.util.UUID

/**
 * BranchCapacity entity for real-time capacity monitoring.
 */
data class BranchCapacity(
    val branchId: UUID,
    val maxCapacity: Int,
    val currentOccupancy: Int,
    val maleCount: Int,
    val femaleCount: Int,
    val lastUpdated: Instant
) {
    init {
        require(maxCapacity > 0) { "Max capacity must be positive" }
        require(currentOccupancy >= 0) { "Current occupancy cannot be negative" }
        require(currentOccupancy <= maxCapacity) {
            "Current occupancy cannot exceed max capacity"
        }
        require(maleCount >= 0) { "Male count cannot be negative" }
        require(femaleCount >= 0) { "Female count cannot be negative" }
        require(maleCount + femaleCount == currentOccupancy) {
            "Male and female counts must sum to current occupancy"
        }
    }

    fun isFull(): Boolean = currentOccupancy >= maxCapacity

    fun getAvailableCapacity(): Int = maxCapacity - currentOccupancy

    fun getOccupancyPercentage(): Double {
        return (currentOccupancy.toDouble() / maxCapacity.toDouble()) * 100
    }

    fun incrementOccupancy(gender: Gender): BranchCapacity {
        require(!isFull()) { "Branch is at full capacity" }
        return when (gender) {
            Gender.MALE -> copy(
                currentOccupancy = currentOccupancy + 1,
                maleCount = maleCount + 1,
                lastUpdated = Instant.now()
            )
            Gender.FEMALE -> copy(
                currentOccupancy = currentOccupancy + 1,
                femaleCount = femaleCount + 1,
                lastUpdated = Instant.now()
            )
        }
    }

    fun decrementOccupancy(gender: Gender): BranchCapacity {
        require(currentOccupancy > 0) { "Cannot decrement occupancy below zero" }
        return when (gender) {
            Gender.MALE -> {
                require(maleCount > 0) { "Cannot decrement male count below zero" }
                copy(
                    currentOccupancy = currentOccupancy - 1,
                    maleCount = maleCount - 1,
                    lastUpdated = Instant.now()
                )
            }
            Gender.FEMALE -> {
                require(femaleCount > 0) { "Cannot decrement female count below zero" }
                copy(
                    currentOccupancy = currentOccupancy - 1,
                    femaleCount = femaleCount - 1,
                    lastUpdated = Instant.now()
                )
            }
        }
    }

    companion object {
        fun create(branchId: UUID, maxCapacity: Int): BranchCapacity {
            return BranchCapacity(
                branchId = branchId,
                maxCapacity = maxCapacity,
                currentOccupancy = 0,
                maleCount = 0,
                femaleCount = 0,
                lastUpdated = Instant.now()
            )
        }
    }
}
