package com.liyaqa.gym.domain.services

import java.util.UUID

/**
 * Service interface for revoking tenant access.
 */
interface TenantAccessRevoker {

    /**
     * Revoke access for all users of a tenant.
     * This should invalidate all active sessions and tokens.
     */
    suspend fun revokeTenantAccess(tenantId: UUID): Result<Unit>

    /**
     * Restore access for all users of a tenant.
     */
    suspend fun restoreTenantAccess(tenantId: UUID): Result<Unit>
}
