package com.liyaqa.gym.domain.events

/**
 * Interface for publishing domain events.
 * Implementations of this interface are responsible for dispatching events
 * to appropriate event handlers or message brokers.
 */
interface EventPublisher {
    /**
     * Publishes a single domain event.
     *
     * @param event The domain event to publish
     */
    fun publish(event: DomainEvent)

    /**
     * Publishes multiple domain events.
     *
     * @param events The collection of domain events to publish
     */
    fun publishAll(events: Collection<DomainEvent>) {
        events.forEach { publish(it) }
    }
}
