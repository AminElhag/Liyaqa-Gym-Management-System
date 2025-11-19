package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member's profile is updated.
 *
 * @property memberId The unique identifier of the member
 * @property branchId The unique identifier of the branch
 * @property updatedFields List of field names that were updated
 * @property updatedBy The user ID who performed the update (null if self-update)
 * @property timestamp The timestamp when the profile was updated
 */
data class MemberProfileUpdatedEvent(
    val memberId: UUID,
    val branchId: UUID,
    val updatedFields: List<String>,
    val updatedBy: UUID?,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "MemberProfileUpdatedEvent(eventId=$eventId, memberId=$memberId, branchId=$branchId, " +
                "updatedFields=$updatedFields, updatedBy=$updatedBy, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
