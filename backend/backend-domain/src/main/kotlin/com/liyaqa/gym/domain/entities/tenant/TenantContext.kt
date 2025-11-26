package com.liyaqa.gym.domain.entities.tenant

import java.util.UUID

/**
 * TenantContext represents the current tenant's operational context.
 * Used throughout the application to enforce tenant isolation and feature access.
 * Typically stored in request scope or thread-local storage.
 */
data class TenantContext(
    val tenantId: UUID,
    val tenantSlug: String,
    val features: Set<PlatformFeature>,
    val limits: TenantLimits
) {
    /**
     * Check if a specific feature is enabled for this tenant
     */
    fun hasFeature(feature: PlatformFeature): Boolean {
        return features.contains(feature)
    }

    /**
     * Check multiple features at once
     */
    fun hasAllFeatures(vararg requiredFeatures: PlatformFeature): Boolean {
        return requiredFeatures.all { features.contains(it) }
    }

    /**
     * Check if at least one of the features is enabled
     */
    fun hasAnyFeature(vararg requiredFeatures: PlatformFeature): Boolean {
        return requiredFeatures.any { features.contains(it) }
    }

    /**
     * Validate if the tenant can perform an action based on current usage
     */
    fun canCreateBranch(currentCount: Int): Boolean {
        return currentCount < limits.maxBranches
    }

    fun canAddMember(currentCount: Int): Boolean {
        return currentCount < limits.maxMembers
    }

    fun canAddStaff(currentCount: Int): Boolean {
        return currentCount < limits.maxStaff
    }

    fun canUseStorage(currentUsageMB: Long, additionalMB: Long): Boolean {
        return (currentUsageMB + additionalMB) <= limits.maxStorageMB
    }

    /**
     * Get remaining capacity for each limit
     */
    fun remainingBranches(currentCount: Int): Int {
        return (limits.maxBranches - currentCount).coerceAtLeast(0)
    }

    fun remainingMembers(currentCount: Int): Int {
        return (limits.maxMembers - currentCount).coerceAtLeast(0)
    }

    fun remainingStaff(currentCount: Int): Int {
        return (limits.maxStaff - currentCount).coerceAtLeast(0)
    }

    fun remainingStorageMB(currentUsageMB: Long): Long {
        return (limits.maxStorageMB - currentUsageMB).coerceAtLeast(0)
    }

    /**
     * Calculate usage percentages
     */
    fun branchUsagePercentage(currentCount: Int): Double {
        return if (limits.maxBranches > 0) {
            (currentCount.toDouble() / limits.maxBranches.toDouble()) * 100
        } else {
            0.0
        }
    }

    fun memberUsagePercentage(currentCount: Int): Double {
        return if (limits.maxMembers > 0) {
            (currentCount.toDouble() / limits.maxMembers.toDouble()) * 100
        } else {
            0.0
        }
    }

    fun staffUsagePercentage(currentCount: Int): Double {
        return if (limits.maxStaff > 0) {
            (currentCount.toDouble() / limits.maxStaff.toDouble()) * 100
        } else {
            0.0
        }
    }

    fun storageUsagePercentage(currentUsageMB: Long): Double {
        return if (limits.maxStorageMB > 0) {
            (currentUsageMB.toDouble() / limits.maxStorageMB.toDouble()) * 100
        } else {
            0.0
        }
    }

    /**
     * Check if any limit is close to being reached (>= 80%)
     */
    fun isNearingLimits(
        currentBranches: Int,
        currentMembers: Int,
        currentStaff: Int,
        currentStorageMB: Long
    ): Boolean {
        return branchUsagePercentage(currentBranches) >= 80 ||
               memberUsagePercentage(currentMembers) >= 80 ||
               staffUsagePercentage(currentStaff) >= 80 ||
               storageUsagePercentage(currentStorageMB) >= 80
    }

    companion object {
        fun fromTenant(tenant: Tenant): TenantContext {
            return TenantContext(
                tenantId = tenant.id,
                tenantSlug = tenant.slug,
                features = tenant.features,
                limits = TenantLimits(
                    maxBranches = tenant.maxBranches,
                    maxMembers = tenant.maxMembers,
                    maxStaff = tenant.maxStaff,
                    maxStorageMB = tenant.subscriptionPlan.defaultMaxStorageMB
                )
            )
        }
    }
}

/**
 * TenantLimits defines the operational limits for a tenant based on their subscription plan
 */
data class TenantLimits(
    val maxBranches: Int,
    val maxMembers: Int,
    val maxStaff: Int,
    val maxStorageMB: Long
) {
    init {
        require(maxBranches > 0) { "Max branches must be positive" }
        require(maxMembers > 0) { "Max members must be positive" }
        require(maxStaff > 0) { "Max staff must be positive" }
        require(maxStorageMB > 0) { "Max storage must be positive" }
    }

    fun maxStorageGB(): Double {
        return maxStorageMB / 1024.0
    }

    companion object {
        fun fromPlan(plan: TenantSubscriptionPlan): TenantLimits {
            return TenantLimits(
                maxBranches = plan.defaultMaxBranches,
                maxMembers = plan.defaultMaxMembers,
                maxStaff = plan.defaultMaxStaff,
                maxStorageMB = plan.defaultMaxStorageMB
            )
        }

        fun unlimited(): TenantLimits {
            return TenantLimits(
                maxBranches = Int.MAX_VALUE,
                maxMembers = Int.MAX_VALUE,
                maxStaff = Int.MAX_VALUE,
                maxStorageMB = Long.MAX_VALUE
            )
        }
    }
}

/**
 * Extension property for TenantSubscriptionPlan to provide default storage limits
 */
val TenantSubscriptionPlan.defaultMaxStorageMB: Long
    get() = when (this) {
        TenantSubscriptionPlan.STARTER -> 5_120L // 5 GB
        TenantSubscriptionPlan.PROFESSIONAL -> 50_120L // 50 GB
        TenantSubscriptionPlan.ENTERPRISE -> 1_048_576L // 1 TB
    }

/**
 * Exception thrown when a tenant exceeds their plan limits
 */
class TenantLimitExceededException(
    val tenantId: UUID,
    val limitType: String,
    val currentValue: Long,
    val maxValue: Long
) : RuntimeException("Tenant $tenantId exceeded $limitType limit: $currentValue / $maxValue")

/**
 * Exception thrown when a tenant tries to access a feature they don't have
 */
class FeatureNotEnabledException(
    val tenantId: UUID,
    val feature: PlatformFeature
) : RuntimeException("Tenant $tenantId does not have access to feature: $feature")
