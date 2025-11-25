package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.MembershipPlan
import com.liyaqa.gym.domain.entities.PlanType
import com.liyaqa.gym.domain.repositories.MembershipPlanRepository
import com.liyaqa.infrastructure.persistence.mappers.MembershipPlanEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Implementation of MembershipPlanRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to MembershipPlanJpaRepository.
 */
@Repository
class MembershipPlanRepositoryImpl(
    private val jpaRepository: MembershipPlanJpaRepository,
    private val mapper: MembershipPlanEntityMapper
) : MembershipPlanRepository {

    private val logger = LoggerFactory.getLogger(MembershipPlanRepositoryImpl::class.java)

    /**
     * Find a membership plan by its unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<MembershipPlan>> {
        return runCatching {
            logger.debug("Finding membership plan by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find membership plan by ID: {}", id, error)
        }
    }

    /**
     * Find all active membership plans for a specific branch.
     */
    override fun findActiveByBranch(branchId: UUID, page: Int, size: Int): Result<List<MembershipPlan>> {
        return runCatching {
            logger.debug("Finding active membership plans for branch: {}, page: {}, size: {}", branchId, page, size)
            val entities = jpaRepository.findActiveByBranchId(branchId)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find active membership plans for branch: {}", branchId, error)
        }
    }

    /**
     * Find all membership plans for a specific branch (including inactive).
     */
    override fun findByBranch(branchId: UUID, page: Int, size: Int): Result<List<MembershipPlan>> {
        return runCatching {
            logger.debug("Finding membership plans for branch: {}, page: {}, size: {}", branchId, page, size)
            val entities = jpaRepository.findByBranchId(branchId)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find membership plans for branch: {}", branchId, error)
        }
    }

    /**
     * Find membership plans by type.
     */
    override fun findByType(type: PlanType, page: Int, size: Int): Result<List<MembershipPlan>> {
        return runCatching {
            logger.debug("Finding membership plans by type: {}, page: {}, size: {}", type, page, size)
            val entities = jpaRepository.findByType(type)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find membership plans by type: {}", type, error)
        }
    }

    /**
     * Save a membership plan (create or update).
     */
    override fun save(plan: MembershipPlan): Result<MembershipPlan> {
        return runCatching {
            logger.debug("Saving membership plan: {}", plan.id)
            val entity = mapper.toEntity(plan)
            val savedEntity = jpaRepository.save(entity)
            mapper.toDomain(savedEntity)
        }.onFailure { error ->
            logger.error("Failed to save membership plan: {}", plan.id, error)
        }
    }

    /**
     * Check if a plan exists by id.
     */
    override fun existsById(id: UUID): Result<Boolean> {
        return runCatching {
            logger.debug("Checking if membership plan exists by ID: {}", id)
            jpaRepository.existsById(id)
        }.onFailure { error ->
            logger.error("Failed to check if membership plan exists by ID: {}", id, error)
        }
    }
}
