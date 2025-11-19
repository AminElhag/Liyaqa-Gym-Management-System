package com.liyaqa.gym.domain.events

import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member checks out from a gym branch.
 *
 * @property memberId The unique identifier of the member checking out
 * @property branchId The unique identifier of the branch where check-out occurred
 * @property duration The duration of the member's gym session
 * @property timestamp The timestamp when the member checked out
 */
data class MemberCheckedOutEvent(
    val memberId: UUID,
    val branchId: UUID,
    val duration: Duration,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "MemberCheckedOutEvent(eventId=$eventId, memberId=$memberId, branchId=$branchId, duration=$duration, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
