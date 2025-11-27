package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.tenant.UsageEvent
import com.liyaqa.infrastructure.persistence.entities.UsageEventJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between UsageEvent domain entity and UsageEventJpaEntity.
 */
@Component
class UsageEventEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: UsageEvent): UsageEventJpaEntity {
        return UsageEventJpaEntity(
            id = domain.id,
            tenantId = domain.tenantId,
            eventType = domain.eventType,
            quantity = domain.quantity,
            metadata = domain.metadata,
            occurredAt = domain.occurredAt,
            createdAt = domain.createdAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: UsageEventJpaEntity): UsageEvent {
        return UsageEvent(
            id = entity.id,
            tenantId = entity.tenantId,
            eventType = entity.eventType,
            quantity = entity.quantity,
            metadata = entity.metadata,
            occurredAt = entity.occurredAt,
            createdAt = entity.createdAt
        )
    }

    /**
     * Convert list of JPA entities to domain entities.
     */
    fun toDomainList(entities: List<UsageEventJpaEntity>): List<UsageEvent> {
        return entities.map { toDomain(it) }
    }

    /**
     * Convert list of domain entities to JPA entities.
     */
    fun toEntityList(domains: List<UsageEvent>): List<UsageEventJpaEntity> {
        return domains.map { toEntity(it) }
    }
}
