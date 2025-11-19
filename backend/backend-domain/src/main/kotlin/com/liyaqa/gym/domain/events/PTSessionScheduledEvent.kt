package com.liyaqa.gym.domain.events

import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

/**
 * Event emitted when a personal training session is scheduled.
 *
 * @property sessionId The unique identifier of the PT session
 * @property trainerId The unique identifier of the trainer
 * @property memberId The unique identifier of the member
 * @property scheduledAt The scheduled date and time for the session
 */
data class PTSessionScheduledEvent(
    val sessionId: UUID,
    val trainerId: UUID,
    val memberId: UUID,
    val scheduledAt: LocalDateTime,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "PTSessionScheduledEvent(eventId=$eventId, sessionId=$sessionId, trainerId=$trainerId, memberId=$memberId, scheduledAt=$scheduledAt, occurredAt=$occurredAt)"
    }
}
