package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when access is denied to a member.
 *
 * @property memberId The unique identifier of the member
 * @property branchId The unique identifier of the branch
 * @property reason The reason for access denial
 * @property timestamp The timestamp when access was denied
 */
data class AccessDeniedEvent(
    val memberId: UUID,
    val branchId: UUID,
    val reason: String,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "AccessDeniedEvent(eventId=$eventId, memberId=$memberId, branchId=$branchId, " +
                "reason=$reason, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
