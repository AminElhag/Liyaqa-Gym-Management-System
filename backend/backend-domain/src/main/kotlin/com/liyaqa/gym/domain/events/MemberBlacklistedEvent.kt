package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member is added to the blacklist.
 *
 * @property memberId The unique identifier of the member
 * @property branchId The unique identifier of the branch (null if system-wide)
 * @property blacklistId The unique identifier of the blacklist entry
 * @property reason The reason for blacklisting
 * @property blacklistedBy The unique identifier of the staff who blacklisted the member
 */
data class MemberBlacklistedEvent(
    val memberId: UUID,
    val branchId: UUID?,
    val blacklistId: UUID,
    val reason: String,
    val blacklistedBy: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "MemberBlacklistedEvent(eventId=$eventId, memberId=$memberId, branchId=$branchId, " +
                "blacklistId=$blacklistId, reason=$reason, blacklistedBy=$blacklistedBy, occurredAt=$occurredAt)"
    }
}
