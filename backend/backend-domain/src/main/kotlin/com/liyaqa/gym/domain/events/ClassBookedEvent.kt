package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event emitted when a member books a class.
 *
 * @property bookingId The unique identifier of the booking
 * @property memberId The unique identifier of the member who booked the class
 * @property scheduleId The unique identifier of the class schedule
 * @property timestamp The timestamp when the booking was made
 */
data class ClassBookedEvent(
    val bookingId: UUID,
    val memberId: UUID,
    val scheduleId: UUID,
    val timestamp: Instant,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "ClassBookedEvent(eventId=$eventId, bookingId=$bookingId, memberId=$memberId, scheduleId=$scheduleId, timestamp=$timestamp, occurredAt=$occurredAt)"
    }
}
