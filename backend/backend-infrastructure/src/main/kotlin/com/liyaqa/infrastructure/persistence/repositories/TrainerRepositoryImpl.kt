package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.ClassType
import com.liyaqa.gym.domain.entities.Trainer
import com.liyaqa.gym.domain.repositories.TrainerRepository
import com.liyaqa.infrastructure.persistence.mappers.TrainerEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

/**
 * Implementation of TrainerRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to TrainerJpaRepository.
 */
@Repository
class TrainerRepositoryImpl(
    private val jpaRepository: TrainerJpaRepository,
    private val mapper: TrainerEntityMapper
) : TrainerRepository {

    private val logger = LoggerFactory.getLogger(TrainerRepositoryImpl::class.java)

    /**
     * Find a trainer by their unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<Trainer>> {
        return runCatching {
            logger.debug("Finding trainer by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find trainer by ID: {}", id, error)
        }
    }

    /**
     * Find all trainers belonging to a specific branch.
     */
    override fun findByBranch(branchId: UUID, page: Int, size: Int): Result<List<Trainer>> {
        return runCatching {
            logger.debug("Finding trainers for branch: {}, page: {}, size: {}", branchId, page, size)
            val entities = jpaRepository.findByBranchId(branchId)

            // Apply pagination manually since the JPA method doesn't support it
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find trainers for branch: {}", branchId, error)
        }
    }

    /**
     * Find available trainers for a specific time range.
     * Available means trainers who are active and not scheduled during the given time.
     *
     * Note: This is a simplified implementation that returns all active trainers
     * for the branch. A complete implementation would check schedule conflicts.
     */
    override fun findAvailable(
        branchId: UUID,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        page: Int,
        size: Int
    ): Result<List<Trainer>> {
        return runCatching {
            logger.debug(
                "Finding available trainers for branch: {}, time range: {} - {}, page: {}, size: {}",
                branchId, startTime, endTime, page, size
            )

            // TODO: Implement schedule conflict checking
            // For now, return all active trainers for the branch
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
            logger.error("Failed to find available trainers for branch: {}", branchId, error)
        }
    }

    /**
     * Find trainers with a specific specialization.
     */
    override fun findBySpecialization(
        specialization: ClassType,
        page: Int,
        size: Int
    ): Result<List<Trainer>> {
        return runCatching {
            logger.debug("Finding trainers with specialization: {}, page: {}, size: {}",
                specialization, page, size)

            val entities = jpaRepository.findBySpecialization(specialization)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find trainers with specialization: {}", specialization, error)
        }
    }

    /**
     * Save a trainer (create or update).
     */
    override fun save(trainer: Trainer): Result<Trainer> {
        return runCatching {
            logger.debug("Saving trainer: {}", trainer.id)
            val entity = mapper.toEntity(trainer)
            val savedEntity = jpaRepository.save(entity)
            mapper.toDomain(savedEntity)
        }.onFailure { error ->
            logger.error("Failed to save trainer: {}", trainer.id, error)
        }
    }
}
