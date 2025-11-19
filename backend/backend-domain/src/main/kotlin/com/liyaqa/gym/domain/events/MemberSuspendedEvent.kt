package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member is suspended.
 *
 * @property memberId The unique identifier of the suspended member
 * @property branchId The unique identifier of the branch
 * @property reason The reason for suspension
 * @property suspendedBy The user ID who performed the suspension
 * @property timestamp The timestamp when the member was suspended
 */
data class MemberSuspendedEvent(
    val memberId: UUID,
    val branchId: UUID,
    val reason: String,
    val suspendedBy: UUID,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "MemberSuspendedEvent(eventId=$eventId, memberId=$memberId, branchId=$branchId, " +
                "reason=$reason, suspendedBy=$suspendedBy, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
