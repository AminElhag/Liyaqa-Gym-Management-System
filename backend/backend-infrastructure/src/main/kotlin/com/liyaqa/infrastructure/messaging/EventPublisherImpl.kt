package com.liyaqa.infrastructure.messaging

import com.liyaqa.gym.domain.events.*
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Implementation of EventPublisher that publishes domain events to Kafka topics
 *
 * Features:
 * - Automatic routing of events to appropriate topics
 * - Synchronous and asynchronous publishing
 * - Retry logic with exponential backoff
 * - Comprehensive error handling and logging
 */
@Component
class EventPublisherImpl(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(EventPublisherImpl::class.java)

    /**
     * Publishes a domain event synchronously
     *
     * @param event The domain event to publish
     * @throws EventPublishException if publishing fails after retries
     */
    @Retryable(
        retryFor = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000)
    )
    override fun publish(event: DomainEvent) {
        val topic = getTopicForEvent(event)
        val key = getKeyForEvent(event)

        try {
            logger.debug("Publishing event to topic {}: {}", topic, event)

            val future = kafkaTemplate.send(topic, key, event)
            val result = future.get(30, TimeUnit.SECONDS)

            logger.info(
                "Successfully published event {} to topic {} partition {}",
                event.eventId,
                topic,
                result.recordMetadata.partition()
            )
        } catch (e: Exception) {
            logger.error("Failed to publish event {} to topic {}: {}", event.eventId, topic, e.message, e)
            throw EventPublishException("Failed to publish event ${event.eventId} to topic $topic", e)
        }
    }

    /**
     * Publishes a domain event asynchronously
     *
     * @param event The domain event to publish
     * @return CompletableFuture that completes when the event is published
     */
    @Retryable(
        retryFor = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000)
    )
    fun publishAsync(event: DomainEvent): CompletableFuture<SendResult<String, Any>> {
        val topic = getTopicForEvent(event)
        val key = getKeyForEvent(event)

        logger.debug("Publishing event asynchronously to topic {}: {}", topic, event)

        val future = kafkaTemplate.send(topic, key, event)

        return future.whenComplete { result, ex ->
            if (ex != null) {
                logger.error("Failed to publish event {} to topic {}: {}", event.eventId, topic, ex.message, ex)
            } else {
                logger.info(
                    "Successfully published event {} to topic {} partition {} offset {}",
                    event.eventId,
                    topic,
                    result.recordMetadata.partition(),
                    result.recordMetadata.offset()
                )
            }
        }
    }

    /**
     * Maps domain events to their corresponding Kafka topics
     */
    private fun getTopicForEvent(event: DomainEvent): String {
        return when (event) {
            // Member events
            is MemberRegisteredEvent -> KafkaConfig.MEMBER_EVENTS_TOPIC

            // Subscription events
            is SubscriptionCreatedEvent -> KafkaConfig.SUBSCRIPTION_EVENTS_TOPIC
            is SubscriptionRenewedEvent -> KafkaConfig.SUBSCRIPTION_EVENTS_TOPIC

            // Booking events
            is ClassBookedEvent -> KafkaConfig.BOOKING_EVENTS_TOPIC
            is ClassCancelledEvent -> KafkaConfig.BOOKING_EVENTS_TOPIC
            is WaitlistJoinedEvent -> KafkaConfig.BOOKING_EVENTS_TOPIC
            is PTSessionScheduledEvent -> KafkaConfig.BOOKING_EVENTS_TOPIC

            // Payment events
            is PaymentProcessedEvent -> KafkaConfig.PAYMENT_EVENTS_TOPIC
            is InvoiceGeneratedEvent -> KafkaConfig.PAYMENT_EVENTS_TOPIC

            // Access events (check-ins/check-outs)
            is MemberCheckedInEvent -> KafkaConfig.ACCESS_EVENTS_TOPIC
            is MemberCheckedOutEvent -> KafkaConfig.ACCESS_EVENTS_TOPIC

            // Notification events
            is MaintenanceRequiredEvent -> KafkaConfig.NOTIFICATION_EVENTS_TOPIC

            else -> {
                logger.warn("Unknown event type: {}. Publishing to notification-events topic", event::class.simpleName)
                KafkaConfig.NOTIFICATION_EVENTS_TOPIC
            }
        }
    }

    /**
     * Generates a partition key for the event to ensure related events go to the same partition
     */
    private fun getKeyForEvent(event: DomainEvent): String {
        return when (event) {
            is MemberRegisteredEvent -> "member-${event.memberId}"
            is MemberCheckedInEvent -> "member-${event.memberId}"
            is MemberCheckedOutEvent -> "member-${event.memberId}"
            is SubscriptionCreatedEvent -> "subscription-${event.subscriptionId}"
            is SubscriptionRenewedEvent -> "subscription-${event.subscriptionId}"
            is ClassBookedEvent -> "booking-${event.bookingId}"
            is ClassCancelledEvent -> "booking-${event.bookingId}"
            is WaitlistJoinedEvent -> "waitlist-${event.scheduleId}"
            is PTSessionScheduledEvent -> "pt-${event.sessionId}"
            is PaymentProcessedEvent -> "payment-${event.paymentId}"
            is InvoiceGeneratedEvent -> "invoice-${event.invoiceId}"
            is MaintenanceRequiredEvent -> "maintenance-${event.equipmentId}"
            else -> "event-${event.eventId}"
        }
    }
}

/**
 * Exception thrown when event publishing fails
 */
class EventPublishException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
