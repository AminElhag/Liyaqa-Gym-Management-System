package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when access is granted to a member.
 *
 * @property memberId The unique identifier of the member
 * @property branchId The unique identifier of the branch
 * @property accessLogId The unique identifier of the access log entry
 * @property accessMethod The method of access (QR, Card, Manual, etc.)
 * @property timestamp The timestamp when access was granted
 */
data class AccessGrantedEvent(
    val memberId: UUID,
    val branchId: UUID,
    val accessLogId: UUID,
    val accessMethod: String,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "AccessGrantedEvent(eventId=$eventId, memberId=$memberId, branchId=$branchId, " +
                "accessLogId=$accessLogId, accessMethod=$accessMethod, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
