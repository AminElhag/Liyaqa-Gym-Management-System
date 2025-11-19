package com.liyaqa.gym.domain.events

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a subscription is upgraded to a different plan.
 *
 * @property subscriptionId The unique identifier of the subscription
 * @property memberId The unique identifier of the member
 * @property oldPlanId The unique identifier of the previous plan
 * @property newPlanId The unique identifier of the new plan
 * @property prorationAmount The prorated amount charged for the upgrade
 */
data class SubscriptionUpgradedEvent(
    val subscriptionId: UUID,
    val memberId: UUID,
    val oldPlanId: UUID,
    val newPlanId: UUID,
    val prorationAmount: Money,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "SubscriptionUpgradedEvent(eventId=$eventId, subscriptionId=$subscriptionId, memberId=$memberId, oldPlanId=$oldPlanId, newPlanId=$newPlanId, prorationAmount=$prorationAmount, occurredAt=$occurredAt)"
    }
}
