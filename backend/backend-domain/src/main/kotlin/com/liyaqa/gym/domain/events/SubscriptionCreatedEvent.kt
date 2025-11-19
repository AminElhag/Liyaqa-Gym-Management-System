package com.liyaqa.gym.domain.events

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Event emitted when a new subscription is created for a member.
 *
 * @property subscriptionId The unique identifier of the created subscription
 * @property memberId The unique identifier of the member
 * @property planId The unique identifier of the membership plan
 * @property startDate The start date of the subscription
 * @property endDate The end date of the subscription
 */
data class SubscriptionCreatedEvent(
    val subscriptionId: UUID,
    val memberId: UUID,
    val planId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "SubscriptionCreatedEvent(eventId=$eventId, subscriptionId=$subscriptionId, memberId=$memberId, planId=$planId, startDate=$startDate, endDate=$endDate, occurredAt=$occurredAt)"
    }
}
