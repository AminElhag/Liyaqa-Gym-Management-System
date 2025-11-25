package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.GuestAccess
import com.liyaqa.gym.domain.repositories.GuestAccessRepository
import com.liyaqa.infrastructure.persistence.mappers.GuestAccessEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Implementation of GuestAccessRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to GuestAccessJpaRepository.
 */
@Repository
class GuestAccessRepositoryImpl(
    private val jpaRepository: GuestAccessJpaRepository,
    private val mapper: GuestAccessEntityMapper
) : GuestAccessRepository {

    private val logger = LoggerFactory.getLogger(GuestAccessRepositoryImpl::class.java)

    /**
     * Find a guest access by its unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<GuestAccess>> {
        return runCatching {
            logger.debug("Finding guest access by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find guest access by ID: {}", id, error)
        }
    }

    /**
     * Find a guest access by QR code.
     */
    override fun findByQRCode(qrCode: String): Result<Optional<GuestAccess>> {
        return runCatching {
            logger.debug("Finding guest access by QR code: {}", qrCode.take(8))
            val entity = jpaRepository.findByQRCode(qrCode)

            if (entity != null) {
                Optional.of(mapper.toDomain(entity))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find guest access by QR code", error)
        }
    }

    /**
     * Find all guest accesses created by a host member.
     */
    override fun findByHostMember(hostMemberId: UUID): Result<List<GuestAccess>> {
        return runCatching {
            logger.debug("Finding all guest accesses for host member: {}", hostMemberId)
            val entities = jpaRepository.findByHostMemberId(hostMemberId)
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find guest accesses for host member: {}", hostMemberId, error)
        }
    }

    /**
     * Find active (unused and valid) guest accesses for a host member.
     */
    override fun findActiveByHostMember(hostMemberId: UUID): Result<List<GuestAccess>> {
        return runCatching {
            logger.debug("Finding active guest accesses for host member: {}", hostMemberId)
            val entities = jpaRepository.findActiveByHostMemberId(hostMemberId, Instant.now())
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find active guest accesses for host member: {}", hostMemberId, error)
        }
    }

    /**
     * Count active guest accesses for a host member.
     */
    override fun countActiveByHostMember(hostMemberId: UUID): Result<Int> {
        return runCatching {
            logger.debug("Counting active guest accesses for host member: {}", hostMemberId)
            val count = jpaRepository.countActiveByHostMemberId(hostMemberId, Instant.now())
            logger.debug("Host member {} has {} active guest accesses", hostMemberId, count)
            count
        }.onFailure { error ->
            logger.error("Failed to count active guest accesses for host member: {}", hostMemberId, error)
        }
    }

    /**
     * Save a guest access (create or update).
     */
    override fun save(guestAccess: GuestAccess): Result<GuestAccess> {
        return runCatching {
            logger.debug("Saving guest access: {}", guestAccess.id)
            val entity = mapper.toEntity(guestAccess)
            val savedEntity = jpaRepository.save(entity)
            val savedDomain = mapper.toDomain(savedEntity)
            logger.info("Saved guest access {} for host member {}", savedDomain.id, savedDomain.hostMemberId)
            savedDomain
        }.onFailure { error ->
            logger.error("Failed to save guest access: {}", guestAccess.id, error)
        }
    }
}
