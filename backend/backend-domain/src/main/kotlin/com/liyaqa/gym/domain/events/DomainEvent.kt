package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Base class for all domain events in the Liyaqa gym management system.
 * Domain events represent significant occurrences within the business domain.
 */
abstract class DomainEvent(
    open val eventId: UUID = UUID.randomUUID(),
    open val occurredAt: Instant = Instant.now()
) {
    /**
     * Returns a string representation of the event for logging purposes.
     */
    abstract override fun toString(): String
}
