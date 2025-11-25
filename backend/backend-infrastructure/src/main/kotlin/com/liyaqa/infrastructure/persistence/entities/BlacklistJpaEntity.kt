package com.liyaqa.infrastructure.persistence.entities

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for Blacklist.
 * Tracks members who are denied access to gym facilities.
 */
@Entity
@Table(
    name = "blacklist",
    indexes = [
        Index(name = "idx_blacklist_member_id", columnList = "member_id"),
        Index(name = "idx_blacklist_branch_id", columnList = "branch_id"),
        Index(name = "idx_blacklist_is_active", columnList = "is_active"),
        Index(name = "idx_blacklist_blacklisted_at", columnList = "blacklisted_at"),
        Index(name = "idx_blacklist_expires_at", columnList = "expires_at")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class BlacklistJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "member_id", nullable = false)
    var memberId: UUID,

    @Column(name = "branch_id")
    var branchId: UUID? = null,

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    var reason: String,

    @Column(name = "blacklisted_at", nullable = false)
    var blacklistedAt: Instant,

    @Column(name = "blacklisted_by", nullable = false)
    var blacklistedBy: UUID,

    @Column(name = "expires_at")
    var expiresAt: Instant? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

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
        reason = "",
        blacklistedAt = Instant.now(),
        blacklistedBy = UUID.randomUUID()
    )
}
