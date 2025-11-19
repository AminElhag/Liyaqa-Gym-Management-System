package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.SubscriptionStatus
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * JPA entity for Subscription.
 * Links a Member to a MembershipPlan, tracking the active membership period.
 */
@Entity
@Table(
    name = "subscriptions",
    indexes = [
        Index(name = "idx_subscription_member_id", columnList = "member_id"),
        Index(name = "idx_subscription_plan_id", columnList = "plan_id"),
        Index(name = "idx_subscription_status", columnList = "status"),
        Index(name = "idx_subscription_start_date", columnList = "start_date"),
        Index(name = "idx_subscription_end_date", columnList = "end_date"),
        Index(name = "idx_subscription_auto_renew", columnList = "auto_renew")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class SubscriptionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "member_id", nullable = false)
    var memberId: UUID,

    @Column(name = "plan_id", nullable = false)
    var planId: UUID,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: SubscriptionStatus,

    @Column(name = "auto_renew", nullable = false)
    var autoRenew: Boolean = false,

    @Column(name = "remaining_visits")
    var remainingVisits: Int? = null,

    @Column(name = "paused_at")
    var pausedAt: LocalDate? = null,

    @Column(name = "paused_until")
    var pausedUntil: LocalDate? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: Instant? = null,

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    var cancellationReason: String? = null,

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
        planId = UUID.randomUUID(),
        startDate = LocalDate.now(),
        status = SubscriptionStatus.ACTIVE
    )
}
