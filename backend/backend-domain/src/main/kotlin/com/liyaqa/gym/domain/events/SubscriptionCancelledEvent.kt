package com.liyaqa.gym.domain.events

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Event emitted when a subscription is cancelled.
 *
 * @property subscriptionId The unique identifier of the cancelled subscription
 * @property memberId The unique identifier of the member
 * @property cancellationReason The reason for cancellation
 * @property refundAmount The amount refunded (if applicable)
 * @property effectiveDate The date when cancellation becomes effective
 */
data class SubscriptionCancelledEvent(
    val subscriptionId: UUID,
    val memberId: UUID,
    val cancellationReason: String?,
    val refundAmount: Money?,
    val effectiveDate: LocalDate,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "SubscriptionCancelledEvent(eventId=$eventId, subscriptionId=$subscriptionId, memberId=$memberId, cancellationReason=$cancellationReason, refundAmount=$refundAmount, effectiveDate=$effectiveDate, occurredAt=$occurredAt)"
    }
}
