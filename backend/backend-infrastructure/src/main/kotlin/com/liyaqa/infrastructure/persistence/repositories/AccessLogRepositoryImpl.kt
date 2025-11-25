package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.AccessLog
import com.liyaqa.gym.domain.repositories.AccessLogRepository
import com.liyaqa.infrastructure.persistence.mappers.AccessLogEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Implementation of AccessLogRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to AccessLogJpaRepository.
 */
@Repository
class AccessLogRepositoryImpl(
    private val jpaRepository: AccessLogJpaRepository,
    private val mapper: AccessLogEntityMapper
) : AccessLogRepository {

    private val logger = LoggerFactory.getLogger(AccessLogRepositoryImpl::class.java)

    /**
     * Find an access log by its unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<AccessLog>> {
        return runCatching {
            logger.debug("Finding access log by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find access log by ID: {}", id, error)
        }
    }

    /**
     * Find all access logs for a specific member.
     */
    override fun findByMember(memberId: UUID, page: Int, size: Int): Result<List<AccessLog>> {
        return runCatching {
            logger.debug("Finding access logs for member: {}, page: {}, size: {}", memberId, page, size)
            val entities = jpaRepository.findByMemberId(memberId)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find access logs for member: {}", memberId, error)
        }
    }

    /**
     * Find all access logs for a specific branch.
     */
    override fun findByBranch(branchId: UUID, page: Int, size: Int): Result<List<AccessLog>> {
        return runCatching {
            logger.debug("Finding access logs for branch: {}, page: {}, size: {}", branchId, page, size)
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
            logger.error("Failed to find access logs for branch: {}", branchId, error)
        }
    }

    /**
     * Find access logs within a date range.
     */
    override fun findByDateRange(
        startDate: Instant,
        endDate: Instant,
        page: Int,
        size: Int
    ): Result<List<AccessLog>> {
        return runCatching {
            logger.debug(
                "Finding access logs by date range: {} - {}, page: {}, size: {}",
                startDate, endDate, page, size
            )

            // Use the existing JPA method for branch date range
            // Since we don't have a method for all branches, we'll fetch all and filter
            // In production, you might want to add a specific query method for this
            logger.warn("findByDateRange is using a workaround. Consider adding a dedicated JPA query method.")

            // For now, return empty list to avoid error
            // This should be implemented properly with a new JPA query method
            emptyList<AccessLog>()
        }.onFailure { error ->
            logger.error("Failed to find access logs by date range", error)
        }
    }

    /**
     * Save an access log (create or update).
     */
    override fun save(accessLog: AccessLog): Result<AccessLog> {
        return runCatching {
            logger.debug("Saving access log: {}", accessLog.id)
            val entity = mapper.toEntity(accessLog)
            val savedEntity = jpaRepository.save(entity)
            mapper.toDomain(savedEntity)
        }.onFailure { error ->
            logger.error("Failed to save access log: {}", accessLog.id, error)
        }
    }

    /**
     * Find active (not checked out) access log for a member at a branch.
     */
    override fun findActiveByMemberAndBranch(memberId: UUID, branchId: UUID): Result<Optional<AccessLog>> {
        return runCatching {
            logger.debug("Finding active access log for member: {} at branch: {}", memberId, branchId)
            val entities = jpaRepository.findActiveByMemberId(memberId)

            // Filter for specific branch
            val activeLog = entities.firstOrNull { it.branchId == branchId }

            if (activeLog != null) {
                Optional.of(mapper.toDomain(activeLog))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find active access log for member: {} at branch: {}", memberId, branchId, error)
        }
    }

    /**
     * Count currently checked in members at a branch.
     */
    override fun countCheckedInByBranch(branchId: UUID): Result<Int> {
        return runCatching {
            logger.debug("Counting checked-in members at branch: {}", branchId)
            val entities = jpaRepository.findCurrentlyCheckedInByBranchId(branchId)
            entities.size
        }.onFailure { error ->
            logger.error("Failed to count checked-in members at branch: {}", branchId, error)
        }
    }
}
