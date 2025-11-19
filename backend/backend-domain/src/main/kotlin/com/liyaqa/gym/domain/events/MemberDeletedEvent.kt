package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member is deleted (GDPR compliance).
 * This is emitted for soft deletes with PII anonymization.
 *
 * @property memberId The unique identifier of the deleted member
 * @property branchId The unique identifier of the branch
 * @property reason The reason for deletion
 * @property deletedBy The user ID who performed the deletion (null if self-deletion)
 * @property dataExported Whether member data was exported before deletion
 * @property timestamp The timestamp when the member was deleted
 */
data class MemberDeletedEvent(
    val memberId: UUID,
    val branchId: UUID,
    val reason: String,
    val deletedBy: UUID?,
    val dataExported: Boolean,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "MemberDeletedEvent(eventId=$eventId, memberId=$memberId, branchId=$branchId, " +
                "reason=$reason, deletedBy=$deletedBy, dataExported=$dataExported, " +
                "timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
