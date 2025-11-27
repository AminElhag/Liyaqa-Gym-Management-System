package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.tenant.UsageEventType
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for UsageEvent.
 * Stores billable usage events for usage-based billing.
 */
@Entity
@Table(
    name = "usage_events",
    indexes = [
        Index(name = "idx_usage_event_tenant_id", columnList = "tenant_id"),
        Index(name = "idx_usage_event_event_type", columnList = "event_type"),
        Index(name = "idx_usage_event_occurred_at", columnList = "occurred_at"),
        Index(name = "idx_usage_event_tenant_type", columnList = "tenant_id,event_type"),
        Index(name = "idx_usage_event_tenant_occurred", columnList = "tenant_id,occurred_at")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class UsageEventJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    var eventType: UsageEventType,

    @Column(name = "quantity", nullable = false)
    var quantity: Int,

    @Column(name = "metadata", columnDefinition = "jsonb")
    @Convert(converter = MapToJsonbConverter::class)
    var metadata: Map<String, String> = emptyMap(),

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
) {
    constructor() : this(
        id = UUID.randomUUID(),
        tenantId = UUID.randomUUID(),
        eventType = UsageEventType.API_CALL,
        quantity = 1,
        occurredAt = Instant.now()
    )
}

/**
 * JPA Converter for Map<String, String> to JSONB
 */
@Converter
class MapToJsonbConverter : AttributeConverter<Map<String, String>, String> {
    private val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: Map<String, String>?): String {
        return if (attribute == null || attribute.isEmpty()) {
            "{}"
        } else {
            objectMapper.writeValueAsString(attribute)
        }
    }

    override fun convertToEntityAttribute(dbData: String?): Map<String, String> {
        return if (dbData.isNullOrBlank() || dbData == "{}") {
            emptyMap()
        } else {
            objectMapper.readValue(dbData, objectMapper.typeFactory.constructMapType(
                Map::class.java,
                String::class.java,
                String::class.java
            ))
        }
    }
}
