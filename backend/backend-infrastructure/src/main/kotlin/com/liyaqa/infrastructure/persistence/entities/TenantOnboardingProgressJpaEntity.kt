package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.tenant.OnboardingStatus
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for TenantOnboardingProgress.
 */
@Entity
@Table(
    name = "tenant_onboarding_progress",
    indexes = [
        Index(name = "idx_onboarding_tenant", columnList = "tenant_id"),
        Index(name = "idx_onboarding_tenant_step", columnList = "tenant_id,step", unique = true)
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class TenantOnboardingProgressJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Column(name = "step", nullable = false, length = 50)
    var step: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: OnboardingStatus,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @Column(name = "data", columnDefinition = "TEXT")
    var dataJson: String? = null, // Stored as JSON string

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    // Hibernate requires a no-arg constructor
    constructor() : this(
        id = UUID.randomUUID(),
        tenantId = UUID.randomUUID(),
        step = "",
        status = OnboardingStatus.NOT_STARTED
    )
}
