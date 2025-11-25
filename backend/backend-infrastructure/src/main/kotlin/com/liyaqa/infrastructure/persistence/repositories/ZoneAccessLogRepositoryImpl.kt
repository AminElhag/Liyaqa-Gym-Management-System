package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.ZoneAccessLog
import com.liyaqa.gym.domain.repositories.ZoneAccessLogRepository
import com.liyaqa.infrastructure.persistence.mappers.ZoneAccessLogEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Implementation of ZoneAccessLogRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to ZoneAccessLogJpaRepository.
 */
@Repository
class ZoneAccessLogRepositoryImpl(
    private val jpaRepository: ZoneAccessLogJpaRepository,
    private val mapper: ZoneAccessLogEntityMapper
) : ZoneAccessLogRepository {

    private val logger = LoggerFactory.getLogger(ZoneAccessLogRepositoryImpl::class.java)

    /**
     * Find a zone access log by its unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<ZoneAccessLog>> {
        return runCatching {
            logger.debug("Finding zone access log by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find zone access log by ID: {}", id, error)
        }
    }

    /**
     * Find all zone access logs for a specific access log.
     */
    override fun findByAccessLog(accessLogId: UUID): Result<List<ZoneAccessLog>> {
        return runCatching {
            logger.debug("Finding zone access logs for access log: {}", accessLogId)
            val entities = jpaRepository.findByAccessLogId(accessLogId)
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find zone access logs for access log: {}", accessLogId, error)
        }
    }

    /**
     * Find all zone access logs for a specific member.
     */
    override fun findByMember(memberId: UUID): Result<List<ZoneAccessLog>> {
        return runCatching {
            logger.debug("Finding zone access logs for member: {}", memberId)
            val entities = jpaRepository.findByMemberId(memberId)
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find zone access logs for member: {}", memberId, error)
        }
    }

    /**
     * Find currently active (no exit time) zone access for a member.
     */
    override fun findActiveByMember(memberId: UUID): Result<List<ZoneAccessLog>> {
        return runCatching {
            logger.debug("Finding active zone access logs for member: {}", memberId)
            val entities = jpaRepository.findActiveByMemberId(memberId)
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find active zone access logs for member: {}", memberId, error)
        }
    }

    /**
     * Save a zone access log (create or update).
     */
    override fun save(zoneAccessLog: ZoneAccessLog): Result<ZoneAccessLog> {
        return runCatching {
            logger.debug("Saving zone access log: {}", zoneAccessLog.id)
            val entity = mapper.toEntity(zoneAccessLog)
            val savedEntity = jpaRepository.save(entity)
            val savedDomain = mapper.toDomain(savedEntity)
            logger.info("Saved zone access log {} for member {} in zone {}",
                savedDomain.id, savedDomain.memberId, savedDomain.zoneId)
            savedDomain
        }.onFailure { error ->
            logger.error("Failed to save zone access log: {}", zoneAccessLog.id, error)
        }
    }
}
