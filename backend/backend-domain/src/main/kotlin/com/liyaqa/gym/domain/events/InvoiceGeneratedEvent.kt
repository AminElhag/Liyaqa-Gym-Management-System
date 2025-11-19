package com.liyaqa.gym.domain.events

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.util.UUID

/**
 * Event emitted when an invoice is generated for a member.
 *
 * @property invoiceId The unique identifier of the invoice
 * @property memberId The unique identifier of the member
 * @property amount The invoice amount (before VAT)
 * @property vatAmount The VAT amount
 * @property timestamp The timestamp when the invoice was generated
 */
data class InvoiceGeneratedEvent(
    val invoiceId: UUID,
    val memberId: UUID,
    val amount: Money,
    val vatAmount: Money,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "InvoiceGeneratedEvent(eventId=$eventId, invoiceId=$invoiceId, memberId=$memberId, amount=$amount, vatAmount=$vatAmount, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
