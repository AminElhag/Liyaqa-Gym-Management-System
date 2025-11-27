package com.liyaqa.gym.domain.entities.tenant

import java.time.Instant
import java.util.UUID

/**
 * UsageEvent entity for tracking billable usage events.
 * Records individual usage events for usage-based billing calculations.
 */
data class UsageEvent(
    val id: UUID,
    val tenantId: UUID,
    val eventType: UsageEventType,
    val quantity: Int,
    val metadata: Map<String, String> = emptyMap(),
    val occurredAt: Instant,
    val createdAt: Instant = Instant.now()
) {
    init {
        require(quantity > 0) { "Quantity must be positive" }
    }

    fun getMetadataValue(key: String): String? {
        return metadata[key]
    }

    fun hasMetadata(key: String): Boolean {
        return metadata.containsKey(key)
    }

    companion object {
        fun createApiCall(
            tenantId: UUID,
            endpoint: String,
            occurredAt: Instant = Instant.now()
        ): UsageEvent {
            return UsageEvent(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                eventType = UsageEventType.API_CALL,
                quantity = 1,
                metadata = mapOf("endpoint" to endpoint),
                occurredAt = occurredAt
            )
        }

        fun createSmsMessage(
            tenantId: UUID,
            recipientCount: Int,
            occurredAt: Instant = Instant.now()
        ): UsageEvent {
            require(recipientCount > 0) { "Recipient count must be positive" }
            return UsageEvent(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                eventType = UsageEventType.SMS_MESSAGE,
                quantity = recipientCount,
                occurredAt = occurredAt
            )
        }

        fun createEmailSent(
            tenantId: UUID,
            recipientCount: Int,
            occurredAt: Instant = Instant.now()
        ): UsageEvent {
            require(recipientCount > 0) { "Recipient count must be positive" }
            return UsageEvent(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                eventType = UsageEventType.EMAIL_SENT,
                quantity = recipientCount,
                occurredAt = occurredAt
            )
        }

        fun createStorageUsage(
            tenantId: UUID,
            sizeMB: Long,
            occurredAt: Instant = Instant.now()
        ): UsageEvent {
            require(sizeMB > 0) { "Storage size must be positive" }
            return UsageEvent(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                eventType = UsageEventType.STORAGE_USAGE,
                quantity = sizeMB.toInt(),
                occurredAt = occurredAt
            )
        }
    }
}

/**
 * Types of billable usage events
 */
enum class UsageEventType {
    API_CALL,       // API endpoint calls
    SMS_MESSAGE,    // SMS messages sent
    EMAIL_SENT,     // Emails sent
    STORAGE_USAGE   // Storage space used (in MB)
}
