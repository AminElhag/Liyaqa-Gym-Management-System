package com.liyaqa.gym.domain.events

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Event emitted when a subscription is frozen/paused.
 *
 * @property subscriptionId The unique identifier of the frozen subscription
 * @property memberId The unique identifier of the member
 * @property freezeStartDate The date when the freeze begins
 * @property freezeEndDate The date when the freeze ends
 * @property newEndDate The new subscription end date after freeze extension
 */
data class SubscriptionFrozenEvent(
    val subscriptionId: UUID,
    val memberId: UUID,
    val freezeStartDate: LocalDate,
    val freezeEndDate: LocalDate,
    val newEndDate: LocalDate,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "SubscriptionFrozenEvent(eventId=$eventId, subscriptionId=$subscriptionId, memberId=$memberId, freezeStartDate=$freezeStartDate, freezeEndDate=$freezeEndDate, newEndDate=$newEndDate, occurredAt=$occurredAt)"
    }
}
