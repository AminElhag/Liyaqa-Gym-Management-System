package com.liyaqa.gym.application.platform.commands

import java.util.UUID

/**
 * Command to cancel a tenant's subscription.
 */
data class CancelTenantSubscriptionCommand(
    val tenantId: UUID,
    val reason: String,
    val cancelledBy: UUID,
    val cancelImmediately: Boolean = false // If false, cancel at end of billing period
) {
    init {
        require(reason.isNotBlank()) { "Cancellation reason cannot be blank" }
    }
}
