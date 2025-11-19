package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when an invoice is submitted to ZATCA for clearance.
 *
 * @property invoiceId The unique identifier of the invoice
 * @property invoiceNumber The invoice number
 * @property timestamp The timestamp when the invoice was submitted
 */
data class InvoiceSubmittedToZATCAEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "InvoiceSubmittedToZATCAEvent(eventId=$eventId, invoiceId=$invoiceId, invoiceNumber=$invoiceNumber, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
