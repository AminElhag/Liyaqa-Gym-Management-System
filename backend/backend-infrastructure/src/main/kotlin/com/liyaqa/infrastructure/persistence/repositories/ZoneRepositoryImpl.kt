package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Zone
import com.liyaqa.gym.domain.repositories.ZoneRepository
import com.liyaqa.infrastructure.persistence.mappers.ZoneEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Implementation of ZoneRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to ZoneJpaRepository.
 */
@Repository
class ZoneRepositoryImpl(
    private val jpaRepository: ZoneJpaRepository,
    private val mapper: ZoneEntityMapper
) : ZoneRepository {

    private val logger = LoggerFactory.getLogger(ZoneRepositoryImpl::class.java)

    /**
     * Find a zone by its unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<Zone>> {
        return runCatching {
            logger.debug("Finding zone by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find zone by ID: {}", id, error)
        }
    }

    /**
     * Find all zones for a specific branch.
     */
    override fun findByBranch(branchId: UUID): Result<List<Zone>> {
        return runCatching {
            logger.debug("Finding all zones for branch: {}", branchId)
            val entities = jpaRepository.findByBranchId(branchId)
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find zones for branch: {}", branchId, error)
        }
    }

    /**
     * Find all active zones for a specific branch.
     */
    override fun findActiveByBranch(branchId: UUID): Result<List<Zone>> {
        return runCatching {
            logger.debug("Finding active zones for branch: {}", branchId)
            val entities = jpaRepository.findActiveByBranchId(branchId)
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find active zones for branch: {}", branchId, error)
        }
    }

    /**
     * Save a zone (create or update).
     */
    override fun save(zone: Zone): Result<Zone> {
        return runCatching {
            logger.debug("Saving zone: {}", zone.id)
            val entity = mapper.toEntity(zone)
            val savedEntity = jpaRepository.save(entity)
            val savedDomain = mapper.toDomain(savedEntity)
            logger.info("Saved zone {} in branch {}", savedDomain.name, savedDomain.branchId)
            savedDomain
        }.onFailure { error ->
            logger.error("Failed to save zone: {}", zone.id, error)
        }
    }

    /**
     * Delete a zone by its identifier.
     */
    override fun deleteById(id: UUID): Result<Unit> {
        return runCatching {
            logger.debug("Deleting zone by ID: {}", id)
            jpaRepository.deleteById(id)
            logger.info("Deleted zone {}", id)
        }.onFailure { error ->
            logger.error("Failed to delete zone by ID: {}", id, error)
        }
    }
}
