package com.liyaqa.gym.domain.events

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a refund is successfully processed.
 *
 * @property refundId The unique identifier of the refund
 * @property paymentId The unique identifier of the original payment
 * @property memberId The unique identifier of the member
 * @property amount The refund amount
 * @property creditNoteNumber The credit note number generated
 * @property timestamp The timestamp when the refund was processed
 */
data class RefundProcessedEvent(
    val refundId: UUID,
    val paymentId: UUID,
    val memberId: UUID,
    val amount: Money,
    val creditNoteNumber: String?,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "RefundProcessedEvent(eventId=$eventId, refundId=$refundId, paymentId=$paymentId, memberId=$memberId, amount=$amount, creditNoteNumber=$creditNoteNumber, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
