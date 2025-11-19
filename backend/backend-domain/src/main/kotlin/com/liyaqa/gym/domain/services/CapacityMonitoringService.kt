package com.liyaqa.gym.domain.services

import com.liyaqa.gym.domain.entities.BranchCapacity
import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.repositories.AccessLogRepository
import com.liyaqa.gym.domain.repositories.BranchCapacityRepository
import com.liyaqa.gym.domain.repositories.BranchRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service for real-time capacity monitoring and management.
 *
 * This service provides:
 * - Real-time occupancy tracking
 * - Capacity alerts and warnings
 * - Occupancy statistics
 * - Capacity forecasting
 * - Gender-based occupancy tracking (KSA compliance)
 *
 * @property branchRepository Repository for branch operations
 * @property branchCapacityRepository Repository for capacity management
 * @property accessLogRepository Repository for access log operations
 */
@Service
class CapacityMonitoringService(
    private val branchRepository: BranchRepository,
    private val branchCapacityRepository: BranchCapacityRepository,
    private val accessLogRepository: AccessLogRepository
) {

    private val logger = LoggerFactory.getLogger(CapacityMonitoringService::class.java)

    companion object {
        private const val HIGH_OCCUPANCY_THRESHOLD = 0.80 // 80%
        private const val VERY_HIGH_OCCUPANCY_THRESHOLD = 0.95 // 95%
    }

    /**
     * Capacity status levels.
     */
    enum class CapacityStatus {
        LOW,        // < 50%
        MODERATE,   // 50-80%
        HIGH,       // 80-95%
        VERY_HIGH,  // 95-100%
        FULL        // 100%
    }

    /**
     * Capacity information DTO.
     */
    data class CapacityInfo(
        val branchId: UUID,
        val currentOccupancy: Int,
        val maxCapacity: Int,
        val availableCapacity: Int,
        val occupancyPercentage: Double,
        val status: CapacityStatus,
        val maleCount: Int,
        val femaleCount: Int,
        val isNearCapacity: Boolean,
        val isFull: Boolean
    )

    /**
     * Gets current capacity information for a branch.
     *
     * @param branchId The branch identifier
     * @return Result containing capacity information
     */
    fun getCurrentCapacity(branchId: UUID): Result<CapacityInfo> {
        return runCatching {
            val capacityOpt = branchCapacityRepository.findByBranchId(branchId)
                .getOrElse { error ->
                    logger.error("Failed to get capacity for branch $branchId: ${error.message}", error)
                    throw error
                }

            if (!capacityOpt.isPresent) {
                // Initialize capacity if not found
                val branch = branchRepository.findById(branchId)
                    .getOrElse { error ->
                        logger.error("Failed to get branch: ${error.message}", error)
                        throw error
                    }
                    .orElseThrow { IllegalArgumentException("Branch not found: $branchId") }

                // Default capacity - should be configured per branch
                val defaultCapacity = 100
                val capacity = BranchCapacity.create(branchId, defaultCapacity)

                val savedCapacity = branchCapacityRepository.save(capacity)
                    .getOrElse { error ->
                        logger.error("Failed to initialize capacity: ${error.message}", error)
                        throw error
                    }

                return@runCatching toCapacityInfo(savedCapacity)
            }

            toCapacityInfo(capacityOpt.get())

        }.onFailure { error ->
            logger.error("Failed to get current capacity: ${error.message}", error)
        }
    }

    /**
     * Checks if a branch is near capacity.
     *
     * @param branchId The branch identifier
     * @return Result containing true if near capacity, false otherwise
     */
    fun isNearCapacity(branchId: UUID): Result<Boolean> {
        return runCatching {
            val capacityInfo = getCurrentCapacity(branchId)
                .getOrThrow()

            capacityInfo.occupancyPercentage >= HIGH_OCCUPANCY_THRESHOLD * 100

        }.onFailure { error ->
            logger.error("Failed to check near capacity: ${error.message}", error)
        }
    }

    /**
     * Gets capacity status level.
     *
     * @param branchId The branch identifier
     * @return Result containing capacity status
     */
    fun getCapacityStatus(branchId: UUID): Result<CapacityStatus> {
        return runCatching {
            val capacityInfo = getCurrentCapacity(branchId)
                .getOrThrow()

            capacityInfo.status

        }.onFailure { error ->
            logger.error("Failed to get capacity status: ${error.message}", error)
        }
    }

    /**
     * Recalculates capacity based on actual checked-in members.
     * Useful for correcting any drift between capacity counter and actual access logs.
     *
     * @param branchId The branch identifier
     * @return Result containing updated capacity info
     */
    fun recalculateCapacity(branchId: UUID): Result<CapacityInfo> {
        return runCatching {
            logger.info("Recalculating capacity for branch: $branchId")

            // Get actual count from access logs
            val actualCount = accessLogRepository.countCheckedInByBranch(branchId)
                .getOrElse { error ->
                    logger.error("Failed to count checked in members: ${error.message}", error)
                    throw error
                }

            // Get current capacity
            val capacityOpt = branchCapacityRepository.findByBranchId(branchId)
                .getOrElse { error ->
                    logger.error("Failed to get capacity: ${error.message}", error)
                    throw error
                }

            if (!capacityOpt.isPresent) {
                throw IllegalStateException("Capacity not initialized for branch: $branchId")
            }

            val capacity = capacityOpt.get()

            // Update capacity with actual count
            // Note: In production, you'd want to also recalculate gender counts
            // by querying access logs with member information
            val updatedCapacity = capacity.copy(
                currentOccupancy = actualCount,
                lastUpdated = java.time.Instant.now()
            )

            val savedCapacity = branchCapacityRepository.save(updatedCapacity)
                .getOrElse { error ->
                    logger.error("Failed to save recalculated capacity: ${error.message}", error)
                    throw error
                }

            logger.info("Recalculated capacity for branch $branchId: $actualCount/${capacity.maxCapacity}")

            toCapacityInfo(savedCapacity)

        }.onFailure { error ->
            logger.error("Failed to recalculate capacity: ${error.message}", error)
        }
    }

    /**
     * Converts BranchCapacity entity to CapacityInfo DTO.
     */
    private fun toCapacityInfo(capacity: BranchCapacity): CapacityInfo {
        val occupancyPercentage = capacity.getOccupancyPercentage()
        val status = determineStatus(occupancyPercentage, capacity.isFull())

        return CapacityInfo(
            branchId = capacity.branchId,
            currentOccupancy = capacity.currentOccupancy,
            maxCapacity = capacity.maxCapacity,
            availableCapacity = capacity.getAvailableCapacity(),
            occupancyPercentage = occupancyPercentage,
            status = status,
            maleCount = capacity.maleCount,
            femaleCount = capacity.femaleCount,
            isNearCapacity = occupancyPercentage >= HIGH_OCCUPANCY_THRESHOLD * 100,
            isFull = capacity.isFull()
        )
    }

    /**
     * Determines capacity status based on occupancy percentage.
     */
    private fun determineStatus(occupancyPercentage: Double, isFull: Boolean): CapacityStatus {
        return when {
            isFull -> CapacityStatus.FULL
            occupancyPercentage >= VERY_HIGH_OCCUPANCY_THRESHOLD * 100 -> CapacityStatus.VERY_HIGH
            occupancyPercentage >= HIGH_OCCUPANCY_THRESHOLD * 100 -> CapacityStatus.HIGH
            occupancyPercentage >= 50 -> CapacityStatus.MODERATE
            else -> CapacityStatus.LOW
        }
    }

    /**
     * Checks if branch can accept more members of a specific gender.
     * Useful for facilities with gender restrictions.
     *
     * @param branchId The branch identifier
     * @param gender The gender to check
     * @return Result containing true if can accept, false otherwise
     */
    fun canAcceptGender(branchId: UUID, gender: Gender): Result<Boolean> {
        return runCatching {
            val capacity = getCurrentCapacity(branchId)
                .getOrThrow()

            // If not full, can accept
            if (!capacity.isFull) {
                return@runCatching true
            }

            // Additional logic could be added here for gender-specific capacity limits
            // For example, male-only facilities might have different capacity rules

            false

        }.onFailure { error ->
            logger.error("Failed to check gender acceptance: ${error.message}", error)
        }
    }
}
