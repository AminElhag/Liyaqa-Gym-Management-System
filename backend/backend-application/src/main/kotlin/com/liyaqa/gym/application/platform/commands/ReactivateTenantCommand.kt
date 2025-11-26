package com.liyaqa.gym.application.platform.commands

import java.util.UUID

/**
 * Command to reactivate a suspended or cancelled tenant.
 */
data class ReactivateTenantCommand(
    val tenantId: UUID,
    val reactivatedBy: UUID,
    val notes: String?
)
