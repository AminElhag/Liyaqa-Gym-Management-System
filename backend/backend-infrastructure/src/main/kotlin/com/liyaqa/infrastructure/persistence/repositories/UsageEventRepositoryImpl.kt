package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.tenant.UsageEvent
import com.liyaqa.gym.domain.entities.tenant.UsageEventType
import com.liyaqa.gym.domain.repositories.UsageEventRepository
import com.liyaqa.infrastructure.persistence.mappers.UsageEventEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.YearMonth
import java.util.Optional
import java.util.UUID

/**
 * Implementation of UsageEventRepository that uses Spring Data JPA.
 */
@Repository
class UsageEventRepositoryImpl(
    private val jpaRepository: UsageEventJpaRepository,
    private val mapper: UsageEventEntityMapper
) : UsageEventRepository {

    private val logger = LoggerFactory.getLogger(UsageEventRepositoryImpl::class.java)

    override fun findById(id: UUID): Result<Optional<UsageEvent>> {
        return runCatching {
            logger.debug("Finding usage event by ID: {}", id)
            val entity = jpaRepository.findById(id)
            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find usage event by ID: {}", id, error)
        }
    }

    override fun findByTenantAndPeriod(tenantId: UUID, period: YearMonth): Result<List<UsageEvent>> {
        return runCatching {
            logger.debug("Finding usage events for tenant: {} and period: {}", tenantId, period)
            val entities = jpaRepository.findByTenantAndPeriod(
                tenantId = tenantId,
                year = period.year,
                month = period.monthValue
            )
            mapper.toDomainList(entities)
        }.onFailure { error ->
            logger.error("Failed to find usage events for tenant: {} and period: {}", tenantId, period, error)
        }
    }

    override fun findByTenantPeriodAndType(
        tenantId: UUID,
        period: YearMonth,
        eventType: UsageEventType
    ): Result<List<UsageEvent>> {
        return runCatching {
            logger.debug("Finding usage events for tenant: {}, period: {}, type: {}", tenantId, period, eventType)
            val entities = jpaRepository.findByTenantPeriodAndType(
                tenantId = tenantId,
                year = period.year,
                month = period.monthValue,
                eventType = eventType
            )
            mapper.toDomainList(entities)
        }.onFailure { error ->
            logger.error("Failed to find usage events for tenant: {}, period: {}, type: {}",
                tenantId, period, eventType, error)
        }
    }

    override fun findByTenantAndDateRange(
        tenantId: UUID,
        startDate: Instant,
        endDate: Instant
    ): Result<List<UsageEvent>> {
        return runCatching {
            logger.debug("Finding usage events for tenant: {} between {} and {}", tenantId, startDate, endDate)
            val entities = jpaRepository.findByTenantAndDateRange(tenantId, startDate, endDate)
            mapper.toDomainList(entities)
        }.onFailure { error ->
            logger.error("Failed to find usage events for tenant: {} in date range", tenantId, error)
        }
    }

    override fun countByTenantPeriodAndType(
        tenantId: UUID,
        period: YearMonth,
        eventType: UsageEventType
    ): Result<Long> {
        return runCatching {
            logger.debug("Counting usage events for tenant: {}, period: {}, type: {}", tenantId, period, eventType)
            jpaRepository.countByTenantPeriodAndType(
                tenantId = tenantId,
                year = period.year,
                month = period.monthValue,
                eventType = eventType
            )
        }.onFailure { error ->
            logger.error("Failed to count usage events for tenant: {}, period: {}, type: {}",
                tenantId, period, eventType, error)
        }
    }

    override fun sumQuantityByTenantPeriodAndType(
        tenantId: UUID,
        period: YearMonth,
        eventType: UsageEventType
    ): Result<Long> {
        return runCatching {
            logger.debug("Summing quantity for tenant: {}, period: {}, type: {}", tenantId, period, eventType)
            jpaRepository.sumQuantityByTenantPeriodAndType(
                tenantId = tenantId,
                year = period.year,
                month = period.monthValue,
                eventType = eventType
            ) ?: 0L
        }.onFailure { error ->
            logger.error("Failed to sum quantity for tenant: {}, period: {}, type: {}",
                tenantId, period, eventType, error)
        }
    }

    @Transactional
    override fun save(event: UsageEvent): Result<UsageEvent> {
        return runCatching {
            logger.debug("Saving usage event: {}", event.id)
            val entity = mapper.toEntity(event)
            val savedEntity = jpaRepository.save(entity)
            mapper.toDomain(savedEntity)
        }.onFailure { error ->
            logger.error("Failed to save usage event: {}", event.id, error)
        }
    }

    @Transactional
    override fun saveAll(events: List<UsageEvent>): Result<List<UsageEvent>> {
        return runCatching {
            logger.debug("Saving {} usage events", events.size)
            val entities = mapper.toEntityList(events)
            val savedEntities = jpaRepository.saveAll(entities)
            mapper.toDomainList(savedEntities)
        }.onFailure { error ->
            logger.error("Failed to save usage events in batch", error)
        }
    }

    @Transactional
    override fun deleteOlderThan(beforeDate: Instant): Result<Long> {
        return runCatching {
            logger.debug("Deleting usage events older than: {}", beforeDate)
            val deletedCount = jpaRepository.deleteByOccurredAtBefore(beforeDate)
            deletedCount.toLong()
        }.onFailure { error ->
            logger.error("Failed to delete old usage events", error)
        }
    }
}
