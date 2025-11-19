package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a guest access code is generated.
 *
 * @property guestAccessId The unique identifier of the guest access
 * @property hostMemberId The unique identifier of the host member
 * @property branchId The unique identifier of the branch
 * @property guestName The name of the guest
 * @property validUntil The expiration timestamp of the access code
 */
data class GuestAccessGeneratedEvent(
    val guestAccessId: UUID,
    val hostMemberId: UUID,
    val branchId: UUID,
    val guestName: String,
    val validUntil: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "GuestAccessGeneratedEvent(eventId=$eventId, guestAccessId=$guestAccessId, " +
                "hostMemberId=$hostMemberId, branchId=$branchId, guestName=$guestName, " +
                "validUntil=$validUntil, occurredAt=$occurredAt)"
    }
}
