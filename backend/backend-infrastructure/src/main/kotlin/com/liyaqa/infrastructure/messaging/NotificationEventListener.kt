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

/**
 * Kafka listener that processes events and sends notifications to members
 *
 * Listens to multiple Kafka topics and determines which events require
 * user notifications (email, SMS, push notifications, etc.)
 */
@Component
class NotificationEventListener(
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(NotificationEventListener::class.java)

    /**
     * Listens to member events for notification triggers
     */
    @KafkaListener(
        topics = [KafkaConfig.MEMBER_EVENTS_TOPIC],
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleMemberEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Received member event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            when (eventType) {
                "MemberRegisteredEvent" -> {
                    val memberId = event.get("memberId")?.asText()
                    logger.info("Sending welcome notification to member {}", memberId)
                    sendWelcomeNotification(memberId)
                }
                else -> {
                    logger.debug("No notification required for event type {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error processing member event: {}", e.message, e)
            // Don't acknowledge - message will be redelivered
        }
    }

    /**
     * Listens to subscription events for notification triggers
     */
    @KafkaListener(
        topics = [KafkaConfig.SUBSCRIPTION_EVENTS_TOPIC],
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleSubscriptionEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Received subscription event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            when (eventType) {
                "SubscriptionCreatedEvent" -> {
                    val memberId = event.get("memberId")?.asText()
                    val planId = event.get("planId")?.asText()
                    logger.info("Sending subscription confirmation to member {}", memberId)
                    sendSubscriptionConfirmation(memberId, planId)
                }
                "SubscriptionRenewedEvent" -> {
                    val memberId = event.get("memberId")?.asText()
                    logger.info("Sending renewal confirmation to member {}", memberId)
                    sendRenewalConfirmation(memberId)
                }
                else -> {
                    logger.debug("No notification required for event type {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error processing subscription event: {}", e.message, e)
        }
    }

    /**
     * Listens to booking events for notification triggers
     */
    @KafkaListener(
        topics = [KafkaConfig.BOOKING_EVENTS_TOPIC],
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleBookingEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Received booking event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            when (eventType) {
                "ClassBookedEvent" -> {
                    val memberId = event.get("memberId")?.asText()
                    val scheduleId = event.get("scheduleId")?.asText()
                    logger.info("Sending booking confirmation to member {}", memberId)
                    sendBookingConfirmation(memberId, scheduleId)
                }
                "ClassCancelledEvent" -> {
                    val memberId = event.get("memberId")?.asText()
                    logger.info("Sending cancellation confirmation to member {}", memberId)
                    sendCancellationNotification(memberId)
                }
                "WaitlistJoinedEvent" -> {
                    val memberId = event.get("memberId")?.asText()
                    logger.info("Sending waitlist confirmation to member {}", memberId)
                    sendWaitlistNotification(memberId)
                }
                "PTSessionScheduledEvent" -> {
                    val memberId = event.get("memberId")?.asText()
                    logger.info("Sending PT session confirmation to member {}", memberId)
                    sendPTSessionNotification(memberId)
                }
                else -> {
                    logger.debug("No notification required for event type {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error processing booking event: {}", e.message, e)
        }
    }

    /**
     * Listens to payment events for notification triggers
     */
    @KafkaListener(
        topics = [KafkaConfig.PAYMENT_EVENTS_TOPIC],
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handlePaymentEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Received payment event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            when (eventType) {
                "PaymentProcessedEvent" -> {
                    val memberId = event.get("memberId")?.asText()
                    val amount = event.get("amount")?.toString()
                    logger.info("Sending payment receipt to member {}", memberId)
                    sendPaymentReceipt(memberId, amount)
                }
                "InvoiceGeneratedEvent" -> {
                    val memberId = event.get("memberId")?.asText()
                    logger.info("Sending invoice to member {}", memberId)
                    sendInvoiceNotification(memberId)
                }
                else -> {
                    logger.debug("No notification required for event type {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error processing payment event: {}", e.message, e)
        }
    }

    /**
     * Listens to notification events for direct notifications
     */
    @KafkaListener(
        topics = [KafkaConfig.NOTIFICATION_EVENTS_TOPIC],
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleNotificationEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug("Received notification event from topic {} partition {} offset {}", topic, partition, offset)

            val event = objectMapper.readTree(message)
            val eventType = determineEventType(event)

            when (eventType) {
                "MaintenanceRequiredEvent" -> {
                    val equipmentId = event.get("equipmentId")?.asText()
                    logger.info("Sending maintenance alert for equipment {}", equipmentId)
                    sendMaintenanceAlert(equipmentId)
                }
                else -> {
                    logger.debug("No notification required for event type {}", eventType)
                }
            }

            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error processing notification event: {}", e.message, e)
        }
    }

    // Helper methods to determine event type
    private fun determineEventType(event: JsonNode): String? {
        // Try to infer event type from the structure
        return when {
            event.has("memberId") && event.has("branchId") && !event.has("subscriptionId") && !event.has("bookingId") -> "MemberRegisteredEvent"
            event.has("subscriptionId") && event.has("startDate") -> "SubscriptionCreatedEvent"
            event.has("subscriptionId") && !event.has("startDate") -> "SubscriptionRenewedEvent"
            event.has("bookingId") -> "ClassBookedEvent"
            event.has("waitlistId") -> "WaitlistJoinedEvent"
            event.has("sessionId") -> "PTSessionScheduledEvent"
            event.has("paymentId") -> "PaymentProcessedEvent"
            event.has("invoiceId") -> "InvoiceGeneratedEvent"
            event.has("equipmentId") -> "MaintenanceRequiredEvent"
            else -> null
        }
    }

    // Notification sender methods (placeholders - would integrate with notification service)
    private fun sendWelcomeNotification(memberId: String?) {
        logger.info("📧 Welcome notification sent to member: {}", memberId)
        // TODO: Integrate with email/SMS/push notification service
    }

    private fun sendSubscriptionConfirmation(memberId: String?, planId: String?) {
        logger.info("📧 Subscription confirmation sent to member: {} for plan: {}", memberId, planId)
    }

    private fun sendRenewalConfirmation(memberId: String?) {
        logger.info("📧 Renewal confirmation sent to member: {}", memberId)
    }

    private fun sendBookingConfirmation(memberId: String?, scheduleId: String?) {
        logger.info("📧 Booking confirmation sent to member: {} for schedule: {}", memberId, scheduleId)
    }

    private fun sendCancellationNotification(memberId: String?) {
        logger.info("📧 Cancellation notification sent to member: {}", memberId)
    }

    private fun sendWaitlistNotification(memberId: String?) {
        logger.info("📧 Waitlist notification sent to member: {}", memberId)
    }

    private fun sendPTSessionNotification(memberId: String?) {
        logger.info("📧 PT session confirmation sent to member: {}", memberId)
    }

    private fun sendPaymentReceipt(memberId: String?, amount: String?) {
        logger.info("📧 Payment receipt sent to member: {} for amount: {}", memberId, amount)
    }

    private fun sendInvoiceNotification(memberId: String?) {
        logger.info("📧 Invoice sent to member: {}", memberId)
    }

    private fun sendMaintenanceAlert(equipmentId: String?) {
        logger.info("🔧 Maintenance alert sent for equipment: {}", equipmentId)
    }
}
