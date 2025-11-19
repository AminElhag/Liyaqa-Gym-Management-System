package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when an invoice is successfully cleared by ZATCA.
 *
 * @property invoiceId The unique identifier of the invoice
 * @property invoiceNumber The invoice number
 * @property zatcaClearanceUUID The ZATCA clearance UUID
 * @property timestamp The timestamp when the invoice was cleared
 */
data class InvoiceClearedByZATCAEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val zatcaClearanceUUID: String,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "InvoiceClearedByZATCAEvent(eventId=$eventId, invoiceId=$invoiceId, invoiceNumber=$invoiceNumber, zatcaClearanceUUID=$zatcaClearanceUUID, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
