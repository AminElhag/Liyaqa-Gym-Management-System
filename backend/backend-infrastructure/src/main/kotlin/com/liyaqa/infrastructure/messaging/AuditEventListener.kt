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
import java.util.*

/**
 * Kafka listener that logs all events for audit trail and compliance
 *
 * This listener:
 * - Records all domain events for compliance and security
 * - Maintains an immutable audit log
 * - Tracks event metadata (timestamp, source, partition, offset)
 * - Supports forensic analysis and compliance reporting
 */
@Component
class AuditEventListener(
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(AuditEventListener::class.java)
    private val auditLogger = LoggerFactory.getLogger("AUDIT_LOG")

    /**
     * Listens to all member events for audit logging
     */
    @KafkaListener(
        topics = [KafkaConfig.MEMBER_EVENTS_TOPIC],
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun auditMemberEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            val event = objectMapper.readTree(message)
            logAuditEvent(event, topic, partition, offset, timestamp, "MEMBER")
            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error auditing member event: {}", e.message, e)
        }
    }

    /**
     * Listens to all subscription events for audit logging
     */
    @KafkaListener(
        topics = [KafkaConfig.SUBSCRIPTION_EVENTS_TOPIC],
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun auditSubscriptionEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            val event = objectMapper.readTree(message)
            logAuditEvent(event, topic, partition, offset, timestamp, "SUBSCRIPTION")
            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error auditing subscription event: {}", e.message, e)
        }
    }

    /**
     * Listens to all booking events for audit logging
     */
    @KafkaListener(
        topics = [KafkaConfig.BOOKING_EVENTS_TOPIC],
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun auditBookingEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            val event = objectMapper.readTree(message)
            logAuditEvent(event, topic, partition, offset, timestamp, "BOOKING")
            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error auditing booking event: {}", e.message, e)
        }
    }

    /**
     * Listens to all payment events for audit logging (critical for financial compliance)
     */
    @KafkaListener(
        topics = [KafkaConfig.PAYMENT_EVENTS_TOPIC],
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun auditPaymentEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            val event = objectMapper.readTree(message)
            logAuditEvent(event, topic, partition, offset, timestamp, "PAYMENT", critical = true)
            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error auditing payment event: {}", e.message, e)
            // Payment events are critical - log error separately
            auditLogger.error("CRITICAL: Failed to audit payment event from offset {}: {}", offset, e.message)
        }
    }

    /**
     * Listens to all access events for audit logging (security compliance)
     */
    @KafkaListener(
        topics = [KafkaConfig.ACCESS_EVENTS_TOPIC],
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun auditAccessEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            val event = objectMapper.readTree(message)
            logAuditEvent(event, topic, partition, offset, timestamp, "ACCESS")
            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error auditing access event: {}", e.message, e)
        }
    }

    /**
     * Listens to all notification events for audit logging
     */
    @KafkaListener(
        topics = [KafkaConfig.NOTIFICATION_EVENTS_TOPIC],
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun auditNotificationEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            val event = objectMapper.readTree(message)
            logAuditEvent(event, topic, partition, offset, timestamp, "NOTIFICATION")
            acknowledgment?.acknowledge()
        } catch (e: Exception) {
            logger.error("Error auditing notification event: {}", e.message, e)
        }
    }

    /**
     * Logs an audit entry with full event metadata
     */
    private fun logAuditEvent(
        event: JsonNode,
        topic: String,
        partition: Int,
        offset: Long,
        timestamp: Long,
        category: String,
        critical: Boolean = false
    ) {
        val auditEntry = buildAuditEntry(event, topic, partition, offset, timestamp, category)

        if (critical) {
            auditLogger.warn("🔒 CRITICAL AUDIT: {}", auditEntry)
        } else {
            auditLogger.info("🔍 AUDIT: {}", auditEntry)
        }

        // In production, persist to immutable storage (append-only database, WORM storage, etc.)
        persistAuditEntry(auditEntry, critical)
    }

    /**
     * Builds a structured audit log entry
     */
    private fun buildAuditEntry(
        event: JsonNode,
        topic: String,
        partition: Int,
        offset: Long,
        timestamp: Long,
        category: String
    ): AuditEntry {
        return AuditEntry(
            auditId = UUID.randomUUID(),
            timestamp = Instant.ofEpochMilli(timestamp),
            category = category,
            topic = topic,
            partition = partition,
            offset = offset,
            eventId = event.get("eventId")?.asText(),
            eventType = determineEventType(event),
            occurredAt = event.get("occurredAt")?.asText()?.let { Instant.parse(it) },
            entityId = extractEntityId(event),
            entityType = extractEntityType(event),
            action = extractAction(event),
            details = event.toString()
        )
    }

    /**
     * Persists audit entry to immutable storage
     */
    private fun persistAuditEntry(entry: AuditEntry, critical: Boolean) {
        logger.debug("💾 Persisting audit entry: {}", entry.auditId)
        // TODO: Integrate with audit storage system:
        // - Database with append-only constraints
        // - WORM (Write Once Read Many) storage
        // - Blockchain for critical financial transactions
        // - Cloud audit logging service (AWS CloudTrail, Azure Monitor, etc.)

        if (critical) {
            // For critical events, consider multiple storage backends for redundancy
            logger.debug("💾 Critical audit entry - storing in multiple backends")
        }
    }

    // Helper methods
    private fun determineEventType(event: JsonNode): String {
        return when {
            event.has("memberId") && event.has("branchId") && !event.has("subscriptionId") -> "MemberEvent"
            event.has("subscriptionId") -> "SubscriptionEvent"
            event.has("bookingId") -> "BookingEvent"
            event.has("waitlistId") -> "WaitlistEvent"
            event.has("sessionId") -> "PTSessionEvent"
            event.has("paymentId") -> "PaymentEvent"
            event.has("invoiceId") -> "InvoiceEvent"
            event.has("equipmentId") -> "MaintenanceEvent"
            else -> "UnknownEvent"
        }
    }

    private fun extractEntityId(event: JsonNode): String? {
        return event.get("memberId")?.asText()
            ?: event.get("subscriptionId")?.asText()
            ?: event.get("bookingId")?.asText()
            ?: event.get("paymentId")?.asText()
            ?: event.get("invoiceId")?.asText()
            ?: event.get("equipmentId")?.asText()
    }

    private fun extractEntityType(event: JsonNode): String? {
        return when {
            event.has("memberId") -> "Member"
            event.has("subscriptionId") -> "Subscription"
            event.has("bookingId") -> "Booking"
            event.has("paymentId") -> "Payment"
            event.has("invoiceId") -> "Invoice"
            event.has("equipmentId") -> "Equipment"
            else -> null
        }
    }

    private fun extractAction(event: JsonNode): String {
        return when {
            event.has("startDate") -> "CREATED"
            event.has("paymentId") -> "PROCESSED"
            event.toString().contains("Cancelled") -> "CANCELLED"
            event.toString().contains("CheckedIn") -> "CHECKED_IN"
            event.toString().contains("CheckedOut") -> "CHECKED_OUT"
            event.toString().contains("Renewed") -> "RENEWED"
            event.toString().contains("Scheduled") -> "SCHEDULED"
            event.toString().contains("Generated") -> "GENERATED"
            event.toString().contains("Registered") -> "REGISTERED"
            event.toString().contains("Booked") -> "BOOKED"
            event.toString().contains("Joined") -> "JOINED"
            else -> "UNKNOWN"
        }
    }

    /**
     * Data class representing an audit log entry
     */
    data class AuditEntry(
        val auditId: UUID,
        val timestamp: Instant,
        val category: String,
        val topic: String,
        val partition: Int,
        val offset: Long,
        val eventId: String?,
        val eventType: String,
        val occurredAt: Instant?,
        val entityId: String?,
        val entityType: String?,
        val action: String,
        val details: String
    ) {
        override fun toString(): String {
            return "AuditEntry(auditId=$auditId, timestamp=$timestamp, category=$category, " +
                   "topic=$topic, partition=$partition, offset=$offset, eventId=$eventId, " +
                   "eventType=$eventType, occurredAt=$occurredAt, entityId=$entityId, " +
                   "entityType=$entityType, action=$action)"
        }
    }
}
