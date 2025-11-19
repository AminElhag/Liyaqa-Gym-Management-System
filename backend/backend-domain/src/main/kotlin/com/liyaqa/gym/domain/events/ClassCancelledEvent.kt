package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member cancels a class booking.
 *
 * @property bookingId The unique identifier of the cancelled booking
 * @property memberId The unique identifier of the member who cancelled
 * @property scheduleId The unique identifier of the class schedule
 * @property reason The reason for cancellation
 * @property timestamp The timestamp when the cancellation occurred
 */
data class ClassCancelledEvent(
    val bookingId: UUID,
    val memberId: UUID,
    val scheduleId: UUID,
    val reason: String,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "ClassCancelledEvent(eventId=$eventId, bookingId=$bookingId, memberId=$memberId, scheduleId=$scheduleId, reason='$reason', timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
