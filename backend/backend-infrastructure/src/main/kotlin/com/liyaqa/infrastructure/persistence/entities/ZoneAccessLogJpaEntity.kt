package com.liyaqa.infrastructure.persistence.entities

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for ZoneAccessLog.
 * Tracks member access to specific zones within a facility.
 */
@Entity
@Table(
    name = "zone_access_logs",
    indexes = [
        Index(name = "idx_zone_access_log_access_log_id", columnList = "access_log_id"),
        Index(name = "idx_zone_access_log_zone_id", columnList = "zone_id"),
        Index(name = "idx_zone_access_log_member_id", columnList = "member_id"),
        Index(name = "idx_zone_access_log_entry_time", columnList = "entry_time"),
        Index(name = "idx_zone_access_log_exit_time", columnList = "exit_time")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class ZoneAccessLogJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "access_log_id", nullable = false)
    var accessLogId: UUID,

    @Column(name = "zone_id", nullable = false)
    var zoneId: UUID,

    @Column(name = "member_id", nullable = false)
    var memberId: UUID,

    @Column(name = "entry_time", nullable = false)
    var entryTime: Instant,

    @Column(name = "exit_time")
    var exitTime: Instant? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    constructor() : this(
        id = UUID.randomUUID(),
        accessLogId = UUID.randomUUID(),
        zoneId = UUID.randomUUID(),
        memberId = UUID.randomUUID(),
        entryTime = Instant.now()
    )
}
