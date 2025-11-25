package com.liyaqa.infrastructure.persistence.entities

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for GuestAccess.
 * Tracks temporary access codes for guests.
 */
@Entity
@Table(
    name = "guest_access",
    indexes = [
        Index(name = "idx_guest_access_host_member_id", columnList = "host_member_id"),
        Index(name = "idx_guest_access_branch_id", columnList = "branch_id"),
        Index(name = "idx_guest_access_qr_code", columnList = "qr_code", unique = true),
        Index(name = "idx_guest_access_is_used", columnList = "is_used"),
        Index(name = "idx_guest_access_valid_from", columnList = "valid_from"),
        Index(name = "idx_guest_access_valid_until", columnList = "valid_until")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class GuestAccessJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "host_member_id", nullable = false)
    var hostMemberId: UUID,

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Column(name = "guest_name", nullable = false)
    var guestName: String,

    @Column(name = "guest_phone")
    var guestPhone: String? = null,

    @Column(name = "qr_code", nullable = false, unique = true)
    var qrCode: String,

    @Column(name = "valid_from", nullable = false)
    var validFrom: Instant,

    @Column(name = "valid_until", nullable = false)
    var validUntil: Instant,

    @Column(name = "is_used", nullable = false)
    var isUsed: Boolean = false,

    @Column(name = "used_at")
    var usedAt: Instant? = null,

    @Column(name = "access_log_id")
    var accessLogId: UUID? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    constructor() : this(
        id = UUID.randomUUID(),
        hostMemberId = UUID.randomUUID(),
        branchId = UUID.randomUUID(),
        guestName = "",
        qrCode = "",
        validFrom = Instant.now(),
        validUntil = Instant.now()
    )
}
