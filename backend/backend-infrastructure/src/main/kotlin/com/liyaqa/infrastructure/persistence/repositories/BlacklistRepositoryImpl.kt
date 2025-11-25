package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Blacklist
import com.liyaqa.gym.domain.repositories.BlacklistRepository
import com.liyaqa.infrastructure.persistence.mappers.BlacklistEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Implementation of BlacklistRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to BlacklistJpaRepository.
 */
@Repository
class BlacklistRepositoryImpl(
    private val jpaRepository: BlacklistJpaRepository,
    private val mapper: BlacklistEntityMapper
) : BlacklistRepository {

    private val logger = LoggerFactory.getLogger(BlacklistRepositoryImpl::class.java)

    /**
     * Find a blacklist entry by its unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<Blacklist>> {
        return runCatching {
            logger.debug("Finding blacklist entry by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find blacklist entry by ID: {}", id, error)
        }
    }

    /**
     * Find active blacklist entries for a member.
     */
    override fun findActiveByMember(memberId: UUID): Result<List<Blacklist>> {
        return runCatching {
            logger.debug("Finding active blacklist entries for member: {}", memberId)
            val entities = jpaRepository.findActiveByMemberId(memberId, Instant.now())
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find active blacklist entries for member: {}", memberId, error)
        }
    }

    /**
     * Find blacklist entries for a member (active and inactive).
     */
    override fun findByMember(memberId: UUID): Result<List<Blacklist>> {
        return runCatching {
            logger.debug("Finding all blacklist entries for member: {}", memberId)
            val entities = jpaRepository.findByMemberId(memberId)
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find blacklist entries for member: {}", memberId, error)
        }
    }

    /**
     * Check if a member is currently blacklisted.
     */
    override fun isBlacklisted(memberId: UUID, branchId: UUID?): Result<Boolean> {
        return runCatching {
            logger.debug("Checking if member {} is blacklisted at branch {}", memberId, branchId)
            val isBlacklisted = jpaRepository.existsActiveBlacklistForMember(memberId, branchId, Instant.now())
            logger.debug("Member {} blacklist status at branch {}: {}", memberId, branchId, isBlacklisted)
            isBlacklisted
        }.onFailure { error ->
            logger.error("Failed to check blacklist status for member: {} at branch: {}", memberId, branchId, error)
        }
    }

    /**
     * Save a blacklist entry (create or update).
     */
    override fun save(blacklist: Blacklist): Result<Blacklist> {
        return runCatching {
            logger.debug("Saving blacklist entry: {}", blacklist.id)
            val entity = mapper.toEntity(blacklist)
            val savedEntity = jpaRepository.save(entity)
            val savedDomain = mapper.toDomain(savedEntity)
            logger.info("Saved blacklist entry {} for member {}", savedDomain.id, savedDomain.memberId)
            savedDomain
        }.onFailure { error ->
            logger.error("Failed to save blacklist entry: {}", blacklist.id, error)
        }
    }
}
