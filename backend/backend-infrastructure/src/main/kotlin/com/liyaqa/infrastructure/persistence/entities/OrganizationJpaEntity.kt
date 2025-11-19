package com.liyaqa.infrastructure.persistence.entities

import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/**
 * JPA entity for Organization.
 * Root entity for the multi-tenant system.
 */
@Entity
@Table(
    name = "organizations",
    indexes = [
        Index(name = "idx_organization_name", columnList = "name")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class OrganizationJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "timezone", nullable = false, length = 100)
    var timezone: String,

    @Embedded
    var settings: OrganizationSettingsEmbeddable,

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
    // Hibernate requires a no-arg constructor
    constructor() : this(
        id = UUID.randomUUID(),
        name = "",
        timezone = "Asia/Riyadh",
        settings = OrganizationSettingsEmbeddable()
    )
}

/**
 * Embeddable for Organization settings.
 */
@Embeddable
data class OrganizationSettingsEmbeddable(
    @Column(name = "default_currency", nullable = false, length = 3)
    var defaultCurrency: String = "SAR",

    @Column(name = "default_language", nullable = false, length = 10)
    var defaultLanguage: String = "ar",

    @Column(name = "working_hours_start", nullable = false, length = 5)
    var workingHoursStart: String = "06:00",

    @Column(name = "working_hours_end", nullable = false, length = 5)
    var workingHoursEnd: String = "23:00",

    @Column(name = "allow_online_booking", nullable = false)
    var allowOnlineBooking: Boolean = true,

    @Column(name = "require_member_approval", nullable = false)
    var requireMemberApproval: Boolean = false,

    @Column(name = "enable_waiting_list", nullable = false)
    var enableWaitingList: Boolean = true,

    @Column(name = "max_advance_booking_days", nullable = false)
    var maxAdvanceBookingDays: Int = 14,

    @Column(name = "cancellation_policy_hours", nullable = false)
    var cancellationPolicyHours: Int = 24,

    @Column(name = "enable_notifications", nullable = false)
    var enableNotifications: Boolean = true,

    @Column(name = "notification_email", length = 255)
    var notificationEmail: String? = null
)
