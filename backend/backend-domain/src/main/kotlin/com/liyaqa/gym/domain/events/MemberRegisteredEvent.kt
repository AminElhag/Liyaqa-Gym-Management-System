package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a new member is registered in the system.
 *
 * @property memberId The unique identifier of the registered member
 * @property branchId The unique identifier of the branch where the member registered
 * @property timestamp The timestamp when the member was registered
 */
data class MemberRegisteredEvent(
    val memberId: UUID,
    val branchId: UUID,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "MemberRegisteredEvent(eventId=$eventId, memberId=$memberId, branchId=$branchId, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
