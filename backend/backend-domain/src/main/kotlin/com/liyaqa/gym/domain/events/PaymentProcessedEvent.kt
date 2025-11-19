package com.liyaqa.gym.domain.events

import com.liyaqa.gym.domain.entities.PaymentMethod
import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a payment is successfully processed.
 *
 * @property paymentId The unique identifier of the payment
 * @property memberId The unique identifier of the member who made the payment
 * @property amount The payment amount
 * @property method The payment method used
 * @property timestamp The timestamp when the payment was processed
 */
data class PaymentProcessedEvent(
    val paymentId: UUID,
    val memberId: UUID,
    val amount: Money,
    val method: PaymentMethod,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "PaymentProcessedEvent(eventId=$eventId, paymentId=$paymentId, memberId=$memberId, amount=$amount, method=$method, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
