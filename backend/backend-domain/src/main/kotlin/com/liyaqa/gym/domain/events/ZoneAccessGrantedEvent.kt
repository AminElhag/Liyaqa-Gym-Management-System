package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member is granted access to a zone.
 *
 * @property memberId The unique identifier of the member
 * @property zoneId The unique identifier of the zone
 * @property accessLogId The unique identifier of the main access log
 * @property timestamp The timestamp when zone access was granted
 */
data class ZoneAccessGrantedEvent(
    val memberId: UUID,
    val zoneId: UUID,
    val accessLogId: UUID,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "ZoneAccessGrantedEvent(eventId=$eventId, memberId=$memberId, zoneId=$zoneId, " +
                "accessLogId=$accessLogId, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
