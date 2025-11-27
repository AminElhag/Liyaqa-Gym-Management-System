package com.liyaqa.gym.domain.entities.tenant

import com.liyaqa.gym.domain.exceptions.TenantContextNotSetException
import java.util.UUID

/**
 * Thread-local storage for tenant context.
 * This holder maintains the current tenant context for the duration of a request.
 *
 * Usage:
 * - Set context at the beginning of request processing (in a filter)
 * - Access context anywhere in the application via TenantContextHolder.get()
 * - Clear context at the end of request processing
 *
 * Thread safety: Each thread maintains its own copy of the tenant context.
 */
object TenantContextHolder {

    private val contextHolder: ThreadLocal<TenantContext> = ThreadLocal()

    /**
     * Set the current tenant context for this thread.
     * Should only be called by infrastructure code (filters).
     *
     * @param context The tenant context to set
     */
    fun set(context: TenantContext) {
        contextHolder.set(context)
    }

    /**
     * Get the current tenant context.
     * Returns null if no context has been set for this thread.
     *
     * @return The current tenant context, or null if not set
     */
    fun get(): TenantContext? {
        return contextHolder.get()
    }

    /**
     * Get the current tenant context, throwing an exception if not set.
     * Use this when you require a tenant context to be present.
     *
     * @return The current tenant context
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun require(): TenantContext {
        return get() ?: throw TenantContextNotSetException(
            "No tenant context found. Ensure the TenantContextFilter is properly configured."
        )
    }

    /**
     * Get the current tenant ID.
     * Convenience method that throws if context is not set.
     *
     * @return The current tenant ID
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun getTenantId(): UUID {
        return require().tenantId
    }

    /**
     * Get the current tenant slug.
     * Convenience method that throws if context is not set.
     *
     * @return The current tenant slug
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun getTenantSlug(): String {
        return require().tenantSlug
    }

    /**
     * Clear the tenant context for this thread.
     * Should always be called in a finally block to prevent context leakage.
     */
    fun clear() {
        contextHolder.remove()
    }

    /**
     * Check if a tenant context has been set for this thread.
     *
     * @return true if a tenant context is set, false otherwise
     */
    fun isSet(): Boolean {
        return contextHolder.get() != null
    }

    /**
     * Check if the current tenant has a specific feature enabled.
     *
     * @param feature The feature to check
     * @return true if the feature is enabled, false otherwise
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun hasFeature(feature: PlatformFeature): Boolean {
        return require().hasFeature(feature)
    }

    /**
     * Check if the current tenant has all of the specified features enabled.
     *
     * @param features The features to check
     * @return true if all features are enabled, false otherwise
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun hasAllFeatures(vararg features: PlatformFeature): Boolean {
        return require().hasAllFeatures(*features)
    }

    /**
     * Check if the current tenant has at least one of the specified features enabled.
     *
     * @param features The features to check
     * @return true if at least one feature is enabled, false otherwise
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun hasAnyFeature(vararg features: PlatformFeature): Boolean {
        return require().hasAnyFeature(*features)
    }

    /**
     * Require a specific feature to be enabled for the current tenant.
     * Throws an exception if the feature is not enabled.
     *
     * @param feature The required feature
     * @throws FeatureNotEnabledException if the feature is not enabled
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun requireFeature(feature: PlatformFeature) {
        val context = require()
        if (!context.hasFeature(feature)) {
            throw FeatureNotEnabledException(context.tenantId, feature)
        }
    }

    /**
     * Check if the current tenant can create a new branch.
     *
     * @param currentCount The current number of branches
     * @return true if the tenant can create more branches, false otherwise
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun canCreateBranch(currentCount: Int): Boolean {
        return require().canCreateBranch(currentCount)
    }

    /**
     * Check if the current tenant can add a new member.
     *
     * @param currentCount The current number of members
     * @return true if the tenant can add more members, false otherwise
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun canAddMember(currentCount: Int): Boolean {
        return require().canAddMember(currentCount)
    }

    /**
     * Check if the current tenant can add a new staff member.
     *
     * @param currentCount The current number of staff
     * @return true if the tenant can add more staff, false otherwise
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun canAddStaff(currentCount: Int): Boolean {
        return require().canAddStaff(currentCount)
    }

    /**
     * Check if the current tenant can use additional storage.
     *
     * @param currentUsageMB Current storage usage in megabytes
     * @param additionalMB Additional storage needed in megabytes
     * @return true if the tenant can use the additional storage, false otherwise
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun canUseStorage(currentUsageMB: Long, additionalMB: Long): Boolean {
        return require().canUseStorage(currentUsageMB, additionalMB)
    }

    /**
     * Check a generic limit for the current tenant.
     *
     * @param limit The limit type (branches, members, staff)
     * @param current The current count
     * @return true if the limit has not been reached, false otherwise
     * @throws TenantContextNotSetException if no tenant context has been set
     */
    fun checkLimit(limit: String, current: Int): Boolean {
        val context = require()
        return when (limit.lowercase()) {
            "branches" -> context.canCreateBranch(current)
            "members" -> context.canAddMember(current)
            "staff" -> context.canAddStaff(current)
            else -> true
        }
    }

    /**
     * Execute a block of code with a specific tenant context.
     * Useful for background tasks or testing.
     * Ensures the context is cleared after execution.
     *
     * @param context The tenant context to use
     * @param block The block of code to execute
     * @return The result of the block execution
     */
    fun <T> executeWithContext(context: TenantContext, block: () -> T): T {
        try {
            set(context)
            return block()
        } finally {
            clear()
        }
    }
}
