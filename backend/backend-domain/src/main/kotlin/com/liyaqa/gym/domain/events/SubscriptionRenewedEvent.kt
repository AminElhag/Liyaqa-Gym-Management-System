package com.liyaqa.gym.domain.events

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Event emitted when an existing subscription is renewed.
 *
 * @property subscriptionId The unique identifier of the renewed subscription
 * @property newEndDate The new end date of the subscription after renewal
 */
data class SubscriptionRenewedEvent(
    val subscriptionId: UUID,
    val newEndDate: LocalDate,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "SubscriptionRenewedEvent(eventId=$eventId, subscriptionId=$subscriptionId, newEndDate=$newEndDate, occurredAt=$occurredAt)"
    }
}
