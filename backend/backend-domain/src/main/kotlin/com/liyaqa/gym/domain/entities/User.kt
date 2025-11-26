package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.util.UUID

/**
 * User entity for authentication and authorization.
 * Can represent both staff members and regular members.
 * Belongs to a Tenant.
 */
data class User(
    val id: UUID,
    val tenantId: UUID, // For direct tenant filtering
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val organizationId: UUID,
    val branchId: UUID?,  // Null for organization-level admins
    val memberId: UUID?,  // Reference to Member entity if role is MEMBER
    val staffId: UUID?,   // Reference to Staff entity if role is STAFF/TRAINER
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val mustChangePassword: Boolean,  // Force password change on next login
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(passwordHash.isNotBlank()) { "Password hash cannot be blank" }
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) { "Invalid email format" }

        // Validate role-specific requirements
        when (role) {
            UserRole.MEMBER -> require(memberId != null) { "MEMBER role requires memberId" }
            UserRole.TRAINER, UserRole.STAFF -> require(staffId != null) { "STAFF/TRAINER role requires staffId" }
            UserRole.ADMIN -> {} // No specific requirements
        }
    }

    fun activate(): User {
        return copy(isActive = true, updatedAt = Instant.now())
    }

    fun deactivate(): User {
        return copy(isActive = false, updatedAt = Instant.now())
    }

    fun verifyEmail(): User {
        return copy(isEmailVerified = true, updatedAt = Instant.now())
    }

    fun updatePassword(newPasswordHash: String): User {
        require(newPasswordHash.isNotBlank()) { "Password hash cannot be blank" }
        return copy(passwordHash = newPasswordHash, mustChangePassword = false, updatedAt = Instant.now())
    }

    fun recordLogin(): User {
        return copy(lastLoginAt = Instant.now(), updatedAt = Instant.now())
    }

    fun hasPermission(permission: String): Boolean {
        return role.permissions.contains(permission)
    }

    companion object {
        fun createMemberUser(
            tenantId: UUID,
            email: String,
            passwordHash: String,
            organizationId: UUID,
            branchId: UUID,
            memberId: UUID
        ): User {
            val now = Instant.now()
            return User(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                email = email,
                passwordHash = passwordHash,
                role = UserRole.MEMBER,
                organizationId = organizationId,
                branchId = branchId,
                memberId = memberId,
                staffId = null,
                isActive = true,
                isEmailVerified = false,
                mustChangePassword = false,
                lastLoginAt = null,
                createdAt = now,
                updatedAt = now
            )
        }

        fun createStaffUser(
            tenantId: UUID,
            email: String,
            passwordHash: String,
            role: UserRole,
            organizationId: UUID,
            branchId: UUID?,
            staffId: UUID
        ): User {
            require(role in listOf(UserRole.STAFF, UserRole.TRAINER, UserRole.ADMIN)) {
                "Invalid role for staff user"
            }
            val now = Instant.now()
            return User(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                email = email,
                passwordHash = passwordHash,
                role = role,
                organizationId = organizationId,
                branchId = branchId,
                memberId = null,
                staffId = staffId,
                isActive = true,
                isEmailVerified = false,
                mustChangePassword = false,
                lastLoginAt = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * User roles with their associated permissions
 */
enum class UserRole(val permissions: Set<String>) {
    MEMBER(
        setOf(
            "member:read:own",
            "member:update:own",
            "booking:create:own",
            "booking:read:own",
            "booking:cancel:own",
            "payment:read:own",
            "payment:create:own"
        )
    ),
    TRAINER(
        setOf(
            "member:read:all",
            "class:read:all",
            "class:create",
            "class:update:own",
            "class:cancel:own",
            "booking:read:all",
            "attendance:mark"
        )
    ),
    STAFF(
        setOf(
            "member:read:all",
            "member:create",
            "member:update:all",
            "member:suspend",
            "booking:read:all",
            "booking:create:all",
            "booking:cancel:all",
            "class:read:all",
            "payment:read:all",
            "payment:create",
            "attendance:mark",
            "access:manage"
        )
    ),
    ADMIN(
        setOf(
            "member:*",
            "staff:*",
            "class:*",
            "booking:*",
            "payment:*",
            "attendance:*",
            "access:*",
            "branch:*",
            "organization:read",
            "organization:update",
            "reports:*"
        )
    )
}
