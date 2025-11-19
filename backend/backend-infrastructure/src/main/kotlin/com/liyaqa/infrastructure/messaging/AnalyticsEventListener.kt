package com.liyaqa.infrastructure.messaging

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Kafka listener that processes events for analytics and metrics
 *
 * Tracks:
 * - Member registration trends
 * - Subscription patterns
 * - Booking statistics
 * - Payment metrics
 * - Access patterns
 * - System usage metrics
 */
@Component
class AnalyticsEventListener(
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(AnalyticsEventListener::class.java)

    // In-memory metrics (in production, these would be stored in a time-series database like InfluxDB or Prometheus)
    private val memberRegistrations = AtomicLong(0)
    private val memberCheckIns = AtomicLong(0)
    private val subscriptionCreations = AtomicLong(0)
    private val classBookings = AtomicLong(0)
    private val paymentsProcessed = AtomicLong(0)
    private val totalRevenue = AtomicLong(0)

    private val eventCountsByType = ConcurrentHashMap<String, AtomicLong>()
    private val eventCountsByHour = ConcurrentHashMap<Int, AtomicLong>()

    /**
     * Listens to member events for analytics
     */
    @KafkaListener(
        topics = [KafkaConfig.MEMBER_EVENTS_TOPIC],
        groupId = "analytics-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun analyzeMemberEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Analyzing member event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            incrementEventCounter(eventType)
            trackEventByHour(event)

            when (eventType) {
                "MemberRegisteredEvent" -> {
                    val count = memberRegistrations.incrementAndGet()
                    val branchId = event.get("branchId")?.asText()
                    logger.info("📊 Analytics: Total member registrations: {} | Branch: {}", count, branchId)
                    trackMemberRegistration(event)
                }
                else -> {
                    logger.debug("Analytics tracked for event type: {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error analyzing member event: {}", e.message, e)
        }
    }

    /**
     * Listens to subscription events for analytics
     */
    @KafkaListener(
        topics = [KafkaConfig.SUBSCRIPTION_EVENTS_TOPIC],
        groupId = "analytics-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun analyzeSubscriptionEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Analyzing subscription event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            incrementEventCounter(eventType)
            trackEventByHour(event)

            when (eventType) {
                "SubscriptionCreatedEvent" -> {
                    val count = subscriptionCreations.incrementAndGet()
                    val planId = event.get("planId")?.asText()
                    logger.info("📊 Analytics: Total subscriptions: {} | Plan: {}", count, planId)
                    trackSubscriptionCreation(event)
                }
                "SubscriptionRenewedEvent" -> {
                    logger.info("📊 Analytics: Subscription renewed")
                    trackSubscriptionRenewal(event)
                }
                else -> {
                    logger.debug("Analytics tracked for event type: {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error analyzing subscription event: {}", e.message, e)
        }
    }

    /**
     * Listens to booking events for analytics
     */
    @KafkaListener(
        topics = [KafkaConfig.BOOKING_EVENTS_TOPIC],
        groupId = "analytics-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun analyzeBookingEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Analyzing booking event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            incrementEventCounter(eventType)
            trackEventByHour(event)

            when (eventType) {
                "ClassBookedEvent" -> {
                    val count = classBookings.incrementAndGet()
                    val scheduleId = event.get("scheduleId")?.asText()
                    logger.info("📊 Analytics: Total class bookings: {} | Schedule: {}", count, scheduleId)
                    trackClassBooking(event)
                }
                "ClassCancelledEvent" -> {
                    logger.info("📊 Analytics: Class booking cancelled")
                    trackClassCancellation(event)
                }
                "WaitlistJoinedEvent" -> {
                    logger.info("📊 Analytics: Member joined waitlist")
                    trackWaitlistJoin(event)
                }
                "PTSessionScheduledEvent" -> {
                    logger.info("📊 Analytics: PT session scheduled")
                    trackPTSession(event)
                }
                else -> {
                    logger.debug("Analytics tracked for event type: {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error analyzing booking event: {}", e.message, e)
        }
    }

    /**
     * Listens to payment events for analytics
     */
    @KafkaListener(
        topics = [KafkaConfig.PAYMENT_EVENTS_TOPIC],
        groupId = "analytics-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun analyzePaymentEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Analyzing payment event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            incrementEventCounter(eventType)
            trackEventByHour(event)

            when (eventType) {
                "PaymentProcessedEvent" -> {
                    val count = paymentsProcessed.incrementAndGet()
                    val amount = event.get("amount")?.get("value")?.asDouble() ?: 0.0
                    val revenue = totalRevenue.addAndGet(amount.toLong())
                    val method = event.get("method")?.asText()
                    logger.info("📊 Analytics: Total payments: {} | Total revenue: {} | Method: {}", count, revenue, method)
                    trackPayment(event)
                }
                "InvoiceGeneratedEvent" -> {
                    logger.info("📊 Analytics: Invoice generated")
                    trackInvoiceGeneration(event)
                }
                else -> {
                    logger.debug("Analytics tracked for event type: {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error analyzing payment event: {}", e.message, e)
        }
    }

    /**
     * Listens to access events for analytics
     */
    @KafkaListener(
        topics = [KafkaConfig.ACCESS_EVENTS_TOPIC],
        groupId = "analytics-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun analyzeAccessEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Analyzing access event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            incrementEventCounter(eventType)
            trackEventByHour(event)

            when (eventType) {
                "MemberCheckedInEvent" -> {
                    val count = memberCheckIns.incrementAndGet()
                    val branchId = event.get("branchId")?.asText()
                    logger.info("📊 Analytics: Total check-ins: {} | Branch: {}", count, branchId)
                    trackCheckIn(event)
                }
                "MemberCheckedOutEvent" -> {
                    logger.info("📊 Analytics: Member checked out")
                    trackCheckOut(event)
                }
                else -> {
                    logger.debug("Analytics tracked for event type: {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error analyzing access event: {}", e.message, e)
        }
    }

    // Helper methods
    private fun determineEventType(event: JsonNode): String? {
        return when {
            event.has("memberId") && event.has("branchId") && !event.has("subscriptionId") && !event.has("bookingId") && event.has("timestamp") -> {
                if (event.toString().contains("checkedIn") || event.toString().contains("CheckIn")) "MemberCheckedInEvent"
                else if (event.toString().contains("checkedOut") || event.toString().contains("CheckOut")) "MemberCheckedOutEvent"
                else "MemberRegisteredEvent"
            }
            event.has("subscriptionId") && event.has("startDate") -> "SubscriptionCreatedEvent"
            event.has("subscriptionId") && !event.has("startDate") -> "SubscriptionRenewedEvent"
            event.has("bookingId") -> "ClassBookedEvent"
            event.has("waitlistId") -> "WaitlistJoinedEvent"
            event.has("sessionId") -> "PTSessionScheduledEvent"
            event.has("paymentId") -> "PaymentProcessedEvent"
            event.has("invoiceId") -> "InvoiceGeneratedEvent"
            else -> "UnknownEvent"
        }
    }

    private fun incrementEventCounter(eventType: String?) {
        eventType?.let {
            eventCountsByType.computeIfAbsent(it) { AtomicLong(0) }.incrementAndGet()
        }
    }

    private fun trackEventByHour(event: JsonNode) {
        val occurredAt = event.get("occurredAt")?.asText()
        occurredAt?.let {
            try {
                val instant = Instant.parse(it)
                val hour = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).hour
                eventCountsByHour.computeIfAbsent(hour) { AtomicLong(0) }.incrementAndGet()
            } catch (e: Exception) {
                logger.warn("Could not parse occurredAt timestamp: {}", occurredAt)
            }
        }
    }

    // Analytics tracking methods (would integrate with analytics database/service)
    private fun trackMemberRegistration(event: JsonNode) {
        logger.debug("💾 Storing member registration analytics: {}", event)
        // TODO: Store in time-series database
    }

    private fun trackSubscriptionCreation(event: JsonNode) {
        logger.debug("💾 Storing subscription creation analytics: {}", event)
    }

    private fun trackSubscriptionRenewal(event: JsonNode) {
        logger.debug("💾 Storing subscription renewal analytics: {}", event)
    }

    private fun trackClassBooking(event: JsonNode) {
        logger.debug("💾 Storing class booking analytics: {}", event)
    }

    private fun trackClassCancellation(event: JsonNode) {
        logger.debug("💾 Storing class cancellation analytics: {}", event)
    }

    private fun trackWaitlistJoin(event: JsonNode) {
        logger.debug("💾 Storing waitlist analytics: {}", event)
    }

    private fun trackPTSession(event: JsonNode) {
        logger.debug("💾 Storing PT session analytics: {}", event)
    }

    private fun trackPayment(event: JsonNode) {
        logger.debug("💾 Storing payment analytics: {}", event)
    }

    private fun trackInvoiceGeneration(event: JsonNode) {
        logger.debug("💾 Storing invoice analytics: {}", event)
    }

    private fun trackCheckIn(event: JsonNode) {
        logger.debug("💾 Storing check-in analytics: {}", event)
    }

    private fun trackCheckOut(event: JsonNode) {
        logger.debug("💾 Storing check-out analytics: {}", event)
    }

    /**
     * Get current analytics metrics (for monitoring endpoints)
     */
    fun getMetrics(): Map<String, Any> {
        return mapOf(
            "memberRegistrations" to memberRegistrations.get(),
            "memberCheckIns" to memberCheckIns.get(),
            "subscriptionCreations" to subscriptionCreations.get(),
            "classBookings" to classBookings.get(),
            "paymentsProcessed" to paymentsProcessed.get(),
            "totalRevenue" to totalRevenue.get(),
            "eventCountsByType" to eventCountsByType.mapValues { it.value.get() },
            "eventCountsByHour" to eventCountsByHour.mapValues { it.value.get() }
        )
    }
}
