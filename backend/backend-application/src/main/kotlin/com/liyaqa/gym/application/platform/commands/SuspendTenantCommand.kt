package com.liyaqa.gym.application.platform.commands

import java.util.UUID

/**
 * Command to suspend a tenant.
 */
data class SuspendTenantCommand(
    val tenantId: UUID,
    val reason: String,
    val suspendedBy: UUID
) {
    init {
        require(reason.isNotBlank()) { "Suspension reason cannot be blank" }
    }
}
