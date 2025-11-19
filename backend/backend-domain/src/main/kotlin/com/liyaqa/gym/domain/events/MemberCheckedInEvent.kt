package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member checks in to a gym branch.
 *
 * @property memberId The unique identifier of the member checking in
 * @property branchId The unique identifier of the branch where check-in occurred
 * @property timestamp The timestamp when the member checked in
 */
data class MemberCheckedInEvent(
    val memberId: UUID,
    val branchId: UUID,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "MemberCheckedInEvent(eventId=$eventId, memberId=$memberId, branchId=$branchId, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
