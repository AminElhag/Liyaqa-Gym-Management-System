package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.UserRole
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for User.
 * Maps to the users table for authentication and authorization.
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_email", columnList = "email"),
        Index(name = "idx_users_organization_id", columnList = "organization_id"),
        Index(name = "idx_users_role", columnList = "role"),
        Index(name = "idx_users_is_active", columnList = "is_active")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "idx_users_email_org_unique",
            columnNames = ["email", "organization_id"]
        )
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class UserJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "email", nullable = false, length = 255)
    var email: String,

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    var role: UserRole,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID,

    @Column(name = "branch_id")
    var branchId: UUID? = null,

    @Column(name = "member_id")
    var memberId: UUID? = null,

    @Column(name = "staff_id")
    var staffId: UUID? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "is_email_verified", nullable = false)
    var isEmailVerified: Boolean = false,

    @Column(name = "must_change_password", nullable = false)
    var mustChangePassword: Boolean = false,

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null,

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
        email = "",
        passwordHash = "",
        role = UserRole.MEMBER,
        organizationId = UUID.randomUUID()
    )
}