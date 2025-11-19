package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.AccessType
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for AccessLog.
 * Tracks member check-ins and check-outs at gym facilities.
 */
@Entity
@Table(
    name = "access_logs",
    indexes = [
        Index(name = "idx_access_log_member_id", columnList = "member_id"),
        Index(name = "idx_access_log_branch_id", columnList = "branch_id"),
        Index(name = "idx_access_log_subscription_id", columnList = "subscription_id"),
        Index(name = "idx_access_log_check_in_time", columnList = "check_in_time"),
        Index(name = "idx_access_log_check_out_time", columnList = "check_out_time"),
        Index(name = "idx_access_log_access_type", columnList = "access_type"),
        Index(name = "idx_access_log_device_id", columnList = "device_id")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class AccessLogJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "member_id", nullable = false)
    var memberId: UUID,

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Column(name = "subscription_id")
    var subscriptionId: UUID? = null,

    @Column(name = "check_in_time", nullable = false)
    var checkInTime: Instant,

    @Column(name = "check_out_time")
    var checkOutTime: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 50)
    var accessType: AccessType,

    @Column(name = "access_point", length = 255)
    var accessPoint: String? = null,

    @Column(name = "device_id", length = 255)
    var deviceId: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "validated_by")
    var validatedBy: UUID? = null,

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    constructor() : this(
        id = UUID.randomUUID(),
        memberId = UUID.randomUUID(),
        branchId = UUID.randomUUID(),
        checkInTime = Instant.now(),
        accessType = AccessType.REGULAR
    )
}
