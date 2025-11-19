package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.PlanType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Where
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for MembershipPlan.
 * Defines membership offerings belonging to a Branch.
 */
@Entity
@Table(
    name = "membership_plans",
    indexes = [
        Index(name = "idx_membership_plan_branch_id", columnList = "branch_id"),
        Index(name = "idx_membership_plan_name", columnList = "name"),
        Index(name = "idx_membership_plan_type", columnList = "type"),
        Index(name = "idx_membership_plan_is_active", columnList = "is_active")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class MembershipPlanJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    var type: PlanType,

    @Embedded
    var price: MoneyEmbeddable,

    @Column(name = "duration_days")
    var durationDays: Int? = null,

    @Column(name = "visit_count")
    var visitCount: Int? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_time_slots", columnDefinition = "jsonb")
    var allowedTimeSlots: List<String>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    var features: List<String> = emptyList(),

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "max_active_subscriptions")
    var maxActiveSubscriptions: Int? = null,

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
        branchId = UUID.randomUUID(),
        name = "",
        type = PlanType.DURATION,
        price = MoneyEmbeddable()
    )
}

/**
 * Embeddable for Money value object.
 */
@Embeddable
data class MoneyEmbeddable(
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String = "SAR"
)
