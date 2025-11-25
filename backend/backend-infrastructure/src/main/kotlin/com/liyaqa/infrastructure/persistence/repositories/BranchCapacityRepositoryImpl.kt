package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.BranchCapacity
import com.liyaqa.gym.domain.repositories.BranchCapacityRepository
import com.liyaqa.infrastructure.persistence.mappers.BranchCapacityEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

/**
 * Implementation of BranchCapacityRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to BranchCapacityJpaRepository.
 * Provides atomic increment/decrement operations for thread-safe capacity management.
 */
@Repository
class BranchCapacityRepositoryImpl(
    private val jpaRepository: BranchCapacityJpaRepository,
    private val mapper: BranchCapacityEntityMapper
) : BranchCapacityRepository {

    private val logger = LoggerFactory.getLogger(BranchCapacityRepositoryImpl::class.java)

    /**
     * Find branch capacity by branch identifier.
     */
    override fun findByBranchId(branchId: UUID): Result<Optional<BranchCapacity>> {
        return runCatching {
            logger.debug("Finding branch capacity by branchId: {}", branchId)
            val entity = jpaRepository.findByBranchId(branchId)

            if (entity != null) {
                Optional.of(mapper.toDomain(entity))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find branch capacity by branchId: {}", branchId, error)
        }
    }

    /**
     * Save branch capacity (create or update).
     */
    override fun save(branchCapacity: BranchCapacity): Result<BranchCapacity> {
        return runCatching {
            logger.debug("Saving branch capacity for branch: {}", branchCapacity.branchId)
            val entity = mapper.toEntity(branchCapacity)
            val savedEntity = jpaRepository.save(entity)
            val savedDomain = mapper.toDomain(savedEntity)
            logger.info(
                "Saved branch capacity for branch {} - occupancy: {}/{}",
                savedDomain.branchId,
                savedDomain.currentOccupancy,
                savedDomain.maxCapacity
            )
            savedDomain
        }.onFailure { error ->
            logger.error("Failed to save branch capacity for branch: {}", branchCapacity.branchId, error)
        }
    }

    /**
     * Atomically increment occupancy for a branch.
     * This uses database-level atomic operations to ensure thread-safety.
     */
    @Transactional
    override fun incrementOccupancy(branchId: UUID, isMale: Boolean): Result<BranchCapacity> {
        return runCatching {
            logger.debug("Incrementing occupancy for branch: {} (isMale: {})", branchId, isMale)

            val updateCount = if (isMale) {
                jpaRepository.incrementMaleOccupancy(branchId)
            } else {
                jpaRepository.incrementFemaleOccupancy(branchId)
            }

            if (updateCount == 0) {
                // Either capacity is full or branch capacity doesn't exist
                val existing = jpaRepository.findByBranchId(branchId)
                if (existing == null) {
                    logger.warn("Branch capacity not found for branch: {}", branchId)
                    throw IllegalStateException("Branch capacity not found for branch: $branchId")
                } else if (existing.currentOccupancy >= existing.maxCapacity) {
                    logger.warn("Cannot increment - branch {} is at full capacity", branchId)
                    throw IllegalStateException("Branch is at full capacity")
                }
                throw IllegalStateException("Failed to increment occupancy")
            }

            // Refresh entity to get updated values
            val updatedEntity = jpaRepository.findByBranchId(branchId)
                ?: throw IllegalStateException("Branch capacity not found after update")

            val updatedDomain = mapper.toDomain(updatedEntity)
            logger.info(
                "Incremented occupancy for branch {} - now at {}/{} (M:{}, F:{})",
                branchId,
                updatedDomain.currentOccupancy,
                updatedDomain.maxCapacity,
                updatedDomain.maleCount,
                updatedDomain.femaleCount
            )
            updatedDomain
        }.onFailure { error ->
            logger.error("Failed to increment occupancy for branch: {}", branchId, error)
        }
    }

    /**
     * Atomically decrement occupancy for a branch.
     * This uses database-level atomic operations to ensure thread-safety.
     */
    @Transactional
    override fun decrementOccupancy(branchId: UUID, isMale: Boolean): Result<BranchCapacity> {
        return runCatching {
            logger.debug("Decrementing occupancy for branch: {} (isMale: {})", branchId, isMale)

            val updateCount = if (isMale) {
                jpaRepository.decrementMaleOccupancy(branchId)
            } else {
                jpaRepository.decrementFemaleOccupancy(branchId)
            }

            if (updateCount == 0) {
                // Either occupancy is already 0 or branch capacity doesn't exist
                val existing = jpaRepository.findByBranchId(branchId)
                if (existing == null) {
                    logger.warn("Branch capacity not found for branch: {}", branchId)
                    throw IllegalStateException("Branch capacity not found for branch: $branchId")
                } else if (existing.currentOccupancy == 0) {
                    logger.warn("Cannot decrement - branch {} occupancy is already 0", branchId)
                    throw IllegalStateException("Occupancy is already 0")
                } else if (isMale && existing.maleCount == 0) {
                    logger.warn("Cannot decrement - branch {} male count is already 0", branchId)
                    throw IllegalStateException("Male count is already 0")
                } else if (!isMale && existing.femaleCount == 0) {
                    logger.warn("Cannot decrement - branch {} female count is already 0", branchId)
                    throw IllegalStateException("Female count is already 0")
                }
                throw IllegalStateException("Failed to decrement occupancy")
            }

            // Refresh entity to get updated values
            val updatedEntity = jpaRepository.findByBranchId(branchId)
                ?: throw IllegalStateException("Branch capacity not found after update")

            val updatedDomain = mapper.toDomain(updatedEntity)
            logger.info(
                "Decremented occupancy for branch {} - now at {}/{} (M:{}, F:{})",
                branchId,
                updatedDomain.currentOccupancy,
                updatedDomain.maxCapacity,
                updatedDomain.maleCount,
                updatedDomain.femaleCount
            )
            updatedDomain
        }.onFailure { error ->
            logger.error("Failed to decrement occupancy for branch: {}", branchId, error)
        }
    }
}
