package com.liyaqa.gym.domain.entities.tenant

import java.time.Instant
import java.util.UUID

/**
 * TenantUser entity representing the main account for a tenant.
 * These are the business owners/admins who manage the gym through the platform.
 * Different from regular User (staff/members) who belong to the tenant's gym.
 */
data class TenantUser(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: TenantUserRole,
    val status: UserStatus,
    val lastLoginAt: Instant?,
    val emailVerified: Boolean,
    val phoneVerified: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean = false
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(phone.isNotBlank()) { "Phone cannot be blank" }
        require(passwordHash.isNotBlank()) { "Password hash cannot be blank" }
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) { "Invalid email format" }
    }

    fun isActive(): Boolean = status == UserStatus.ACTIVE

    fun isSuspended(): Boolean = status == UserStatus.SUSPENDED

    fun isOwner(): Boolean = role == TenantUserRole.OWNER

    fun isAdmin(): Boolean = role == TenantUserRole.ADMIN

    fun isManager(): Boolean = role == TenantUserRole.MANAGER

    fun activate(): TenantUser {
        require(status == UserStatus.SUSPENDED || status == UserStatus.PENDING) {
            "Only suspended or pending users can be activated"
        }
        return copy(status = UserStatus.ACTIVE, updatedAt = Instant.now())
    }

    fun suspend(): TenantUser {
        require(status == UserStatus.ACTIVE) { "Only active users can be suspended" }
        return copy(status = UserStatus.SUSPENDED, updatedAt = Instant.now())
    }

    fun verifyEmail(): TenantUser {
        return copy(emailVerified = true, updatedAt = Instant.now())
    }

    fun verifyPhone(): TenantUser {
        return copy(phoneVerified = true, updatedAt = Instant.now())
    }

    fun updatePassword(newPasswordHash: String): TenantUser {
        require(newPasswordHash.isNotBlank()) { "Password hash cannot be blank" }
        return copy(passwordHash = newPasswordHash, updatedAt = Instant.now())
    }

    fun recordLogin(): TenantUser {
        return copy(lastLoginAt = Instant.now(), updatedAt = Instant.now())
    }

    fun updateProfile(name: String, phone: String): TenantUser {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(phone.isNotBlank()) { "Phone cannot be blank" }
        return copy(name = name, phone = phone, updatedAt = Instant.now())
    }

    fun changeRole(newRole: TenantUserRole): TenantUser {
        require(role != TenantUserRole.OWNER || newRole == TenantUserRole.OWNER) {
            "Cannot change role from OWNER"
        }
        return copy(role = newRole, updatedAt = Instant.now())
    }

    fun softDelete(): TenantUser {
        require(role != TenantUserRole.OWNER) { "Cannot delete the owner user" }
        return copy(isDeleted = true, status = UserStatus.SUSPENDED, updatedAt = Instant.now())
    }

    fun hasPermission(permission: TenantPermission): Boolean {
        return role.permissions.contains(permission)
    }

    companion object {
        fun createOwner(
            tenantId: UUID,
            name: String,
            email: String,
            phone: String,
            passwordHash: String
        ): TenantUser {
            val now = Instant.now()
            return TenantUser(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                name = name,
                email = email,
                phone = phone,
                passwordHash = passwordHash,
                role = TenantUserRole.OWNER,
                status = UserStatus.ACTIVE,
                lastLoginAt = null,
                emailVerified = false,
                phoneVerified = false,
                createdAt = now,
                updatedAt = now,
                isDeleted = false
            )
        }

        fun createAdmin(
            tenantId: UUID,
            name: String,
            email: String,
            phone: String,
            passwordHash: String
        ): TenantUser {
            val now = Instant.now()
            return TenantUser(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                name = name,
                email = email,
                phone = phone,
                passwordHash = passwordHash,
                role = TenantUserRole.ADMIN,
                status = UserStatus.PENDING,
                lastLoginAt = null,
                emailVerified = false,
                phoneVerified = false,
                createdAt = now,
                updatedAt = now,
                isDeleted = false
            )
        }

        fun createManager(
            tenantId: UUID,
            name: String,
            email: String,
            phone: String,
            passwordHash: String
        ): TenantUser {
            val now = Instant.now()
            return TenantUser(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                name = name,
                email = email,
                phone = phone,
                passwordHash = passwordHash,
                role = TenantUserRole.MANAGER,
                status = UserStatus.PENDING,
                lastLoginAt = null,
                emailVerified = false,
                phoneVerified = false,
                createdAt = now,
                updatedAt = now,
                isDeleted = false
            )
        }
    }
}

/**
 * Tenant user roles with hierarchical permissions
 */
enum class TenantUserRole(val permissions: Set<TenantPermission>) {
    OWNER(
        // Full access to everything
        setOf(
            TenantPermission.TENANT_MANAGE,
            TenantPermission.SUBSCRIPTION_MANAGE,
            TenantPermission.BILLING_MANAGE,
            TenantPermission.USERS_MANAGE,
            TenantPermission.BRANCHES_MANAGE,
            TenantPermission.MEMBERS_MANAGE,
            TenantPermission.STAFF_MANAGE,
            TenantPermission.SETTINGS_MANAGE,
            TenantPermission.REPORTS_VIEW,
            TenantPermission.INTEGRATIONS_MANAGE
        )
    ),
    ADMIN(
        // Can manage settings and users
        setOf(
            TenantPermission.USERS_MANAGE,
            TenantPermission.BRANCHES_MANAGE,
            TenantPermission.MEMBERS_MANAGE,
            TenantPermission.STAFF_MANAGE,
            TenantPermission.SETTINGS_MANAGE,
            TenantPermission.REPORTS_VIEW
        )
    ),
    MANAGER(
        // Can manage operations only
        setOf(
            TenantPermission.BRANCHES_MANAGE,
            TenantPermission.MEMBERS_MANAGE,
            TenantPermission.STAFF_MANAGE,
            TenantPermission.REPORTS_VIEW
        )
    )
}

/**
 * Tenant-level permissions for access control
 */
enum class TenantPermission {
    TENANT_MANAGE,          // Manage tenant settings (name, logo, etc.)
    SUBSCRIPTION_MANAGE,    // Upgrade/downgrade subscription plans
    BILLING_MANAGE,         // View and manage billing
    USERS_MANAGE,           // Manage tenant users (admins, managers)
    BRANCHES_MANAGE,        // Create and manage branches
    MEMBERS_MANAGE,         // Manage gym members
    STAFF_MANAGE,           // Manage staff and trainers
    SETTINGS_MANAGE,        // Manage organization settings
    REPORTS_VIEW,           // View reports and analytics
    INTEGRATIONS_MANAGE     // Manage integrations and API keys
}

/**
 * User status enumeration
 */
enum class UserStatus {
    ACTIVE,
    SUSPENDED,
    PENDING
}
