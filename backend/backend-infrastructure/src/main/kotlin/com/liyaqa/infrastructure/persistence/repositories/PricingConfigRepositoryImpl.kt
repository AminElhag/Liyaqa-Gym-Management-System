package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.tenant.PricingConfig
import com.liyaqa.gym.domain.repositories.PricingConfigRepository
import com.liyaqa.infrastructure.persistence.mappers.PricingConfigEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Implementation of PricingConfigRepository that uses Spring Data JPA.
 */
@Repository
class PricingConfigRepositoryImpl(
    private val jpaRepository: PricingConfigJpaRepository,
    private val mapper: PricingConfigEntityMapper
) : PricingConfigRepository {

    private val logger = LoggerFactory.getLogger(PricingConfigRepositoryImpl::class.java)

    override fun findById(id: UUID): Result<Optional<PricingConfig>> {
        return runCatching {
            logger.debug("Finding pricing config by ID: {}", id)
            val entity = jpaRepository.findById(id)
            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find pricing config by ID: {}", id, error)
        }
    }

    override fun findActivePricing(): Result<Optional<PricingConfig>> {
        return runCatching {
            logger.debug("Finding active pricing configuration")
            val entities = jpaRepository.findActivePricing()
            if (entities.isNotEmpty()) {
                Optional.of(mapper.toDomain(entities.first()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find active pricing configuration", error)
        }
    }

    override fun findByEffectiveDate(effectiveAt: Instant): Result<Optional<PricingConfig>> {
        return runCatching {
            logger.debug("Finding pricing config effective at: {}", effectiveAt)
            val entities = jpaRepository.findByEffectiveDate(effectiveAt)
            if (entities.isNotEmpty()) {
                Optional.of(mapper.toDomain(entities.first()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find pricing config by effective date: {}", effectiveAt, error)
        }
    }

    override fun findAllActive(): Result<List<PricingConfig>> {
        return runCatching {
            logger.debug("Finding all active pricing configurations")
            val entities = jpaRepository.findAllActive()
            mapper.toDomainList(entities)
        }.onFailure { error ->
            logger.error("Failed to find all active pricing configurations", error)
        }
    }

    override fun findAll(page: Int, size: Int): Result<List<PricingConfig>> {
        return runCatching {
            logger.debug("Finding all pricing configurations, page: {}, size: {}", page, size)
            val entities = jpaRepository.findAll()

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                mapper.toDomainList(entities.subList(startIndex, endIndex))
            }
        }.onFailure { error ->
            logger.error("Failed to find all pricing configurations", error)
        }
    }

    @Transactional
    override fun save(config: PricingConfig): Result<PricingConfig> {
        return runCatching {
            logger.debug("Saving pricing config: {}", config.id)
            val entity = mapper.toEntity(config)
            val savedEntity = jpaRepository.save(entity)
            mapper.toDomain(savedEntity)
        }.onFailure { error ->
            logger.error("Failed to save pricing config: {}", config.id, error)
        }
    }

    @Transactional
    override fun deactivateAll(): Result<Int> {
        return runCatching {
            logger.debug("Deactivating all pricing configurations")
            jpaRepository.deactivateAll()
        }.onFailure { error ->
            logger.error("Failed to deactivate all pricing configurations", error)
        }
    }

    override fun hasActivePricing(): Result<Boolean> {
        return runCatching {
            logger.debug("Checking if active pricing exists")
            jpaRepository.hasActivePricing()
        }.onFailure { error ->
            logger.error("Failed to check if active pricing exists", error)
        }
    }
}
