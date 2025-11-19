package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member joins a class waitlist.
 *
 * @property memberId The unique identifier of the member joining the waitlist
 * @property scheduleId The unique identifier of the class schedule
 * @property position The position of the member in the waitlist
 */
data class WaitlistJoinedEvent(
    val memberId: UUID,
    val scheduleId: UUID,
    val position: Int,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "WaitlistJoinedEvent(eventId=$eventId, memberId=$memberId, scheduleId=$scheduleId, position=$position, occurredAt=$occurredAt)"
    }
}
