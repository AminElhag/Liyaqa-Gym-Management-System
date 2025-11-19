package com.liyaqa.gym.presentation.security

import java.util.UUID

/**
 * Thread-local storage for multi-tenant context
 * Stores current organization and branch IDs for the request
 */
object TenantContext {

    private val organizationId = ThreadLocal<UUID>()
    private val branchId = ThreadLocal<UUID?>()

    /**
     * Set the current organization ID
     */
    fun setOrganizationId(id: UUID) {
        organizationId.set(id)
    }

    /**
     * Get the current organization ID
     */
    fun getOrganizationId(): UUID? {
        return organizationId.get()
    }

    /**
     * Set the current branch ID
     */
    fun setBranchId(id: UUID?) {
        branchId.set(id)
    }

    /**
     * Get the current branch ID
     */
    fun getBranchId(): UUID? {
        return branchId.get()
    }

    /**
     * Clear the tenant context (should be called at the end of request processing)
     */
    fun clear() {
        organizationId.remove()
        branchId.remove()
    }

    /**
     * Check if organization ID is set
     */
    fun hasOrganizationId(): Boolean {
        return organizationId.get() != null
    }

    /**
     * Check if branch ID is set
     */
    fun hasBranchId(): Boolean {
        return branchId.get() != null
    }
}

/**
 * Data class representing tenant information
 */
data class TenantInfo(
    val organizationId: UUID,
    val branchId: UUID?
)
