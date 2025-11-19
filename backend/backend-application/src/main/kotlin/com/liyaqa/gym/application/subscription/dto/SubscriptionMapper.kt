package com.liyaqa.gym.application.subscription.dto

import com.liyaqa.gym.domain.entities.Subscription
import org.springframework.stereotype.Component

/**
 * Mapper for converting between Subscription domain entities and DTOs.
 */
@Component
class SubscriptionMapper {

    /**
     * Convert a Subscription entity to a complete DTO.
     */
    fun toDTO(subscription: Subscription): SubscriptionDTO {
        return SubscriptionDTO(
            id = subscription.id,
            memberId = subscription.memberId,
            planId = subscription.planId,
            startDate = subscription.startDate,
            endDate = subscription.endDate,
            status = subscription.status,
            autoRenew = subscription.autoRenew,
            remainingVisits = subscription.remainingVisits,
            pausedAt = subscription.pausedAt,
            pausedUntil = subscription.pausedUntil,
            cancelledAt = subscription.cancelledAt,
            cancellationReason = subscription.cancellationReason,
            isActive = subscription.isActive(),
            isExpired = subscription.isExpired(),
            canUse = subscription.canUse(),
            createdAt = subscription.createdAt,
            updatedAt = subscription.updatedAt
        )
    }

    /**
     * Convert a Subscription entity to a summary DTO.
     */
    fun toSummaryDTO(subscription: Subscription): SubscriptionSummaryDTO {
        return SubscriptionSummaryDTO(
            id = subscription.id,
            memberId = subscription.memberId,
            planId = subscription.planId,
            startDate = subscription.startDate,
            endDate = subscription.endDate,
            status = subscription.status,
            autoRenew = subscription.autoRenew,
            canUse = subscription.canUse()
        )
    }

    /**
     * Convert a list of subscriptions to DTOs.
     */
    fun toDTOList(subscriptions: List<Subscription>): List<SubscriptionDTO> {
        return subscriptions.map { toDTO(it) }
    }

    /**
     * Convert a list of subscriptions to summary DTOs.
     */
    fun toSummaryDTOList(subscriptions: List<Subscription>): List<SubscriptionSummaryDTO> {
        return subscriptions.map { toSummaryDTO(it) }
    }
}
