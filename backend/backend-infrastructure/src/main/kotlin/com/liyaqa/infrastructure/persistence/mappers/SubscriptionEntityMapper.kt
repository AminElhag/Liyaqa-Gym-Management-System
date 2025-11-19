package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Subscription
import com.liyaqa.infrastructure.persistence.entities.SubscriptionJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between Subscription domain entity and SubscriptionJpaEntity.
 */
@Component
class SubscriptionEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Subscription): SubscriptionJpaEntity {
        return SubscriptionJpaEntity(
            id = domain.id,
            memberId = domain.memberId,
            planId = domain.planId,
            startDate = domain.startDate,
            endDate = domain.endDate,
            status = domain.status,
            autoRenew = domain.autoRenew,
            remainingVisits = domain.remainingVisits,
            pausedAt = domain.pausedAt,
            pausedUntil = domain.pausedUntil,
            cancelledAt = domain.cancelledAt,
            cancellationReason = domain.cancellationReason,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: SubscriptionJpaEntity): Subscription {
        return Subscription(
            id = entity.id,
            memberId = entity.memberId,
            planId = entity.planId,
            startDate = entity.startDate,
            endDate = entity.endDate,
            status = entity.status,
            autoRenew = entity.autoRenew,
            remainingVisits = entity.remainingVisits,
            pausedAt = entity.pausedAt,
            pausedUntil = entity.pausedUntil,
            cancelledAt = entity.cancelledAt,
            cancellationReason = entity.cancellationReason,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
