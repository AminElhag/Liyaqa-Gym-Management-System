package com.liyaqa.gym.application.platform.commands

import com.liyaqa.gym.domain.entities.tenant.TenantSubscriptionPlan
import java.util.UUID

/**
 * Command to downgrade a tenant's subscription plan.
 */
data class DowngradeTenantPlanCommand(
    val tenantId: UUID,
    val newPlan: TenantSubscriptionPlan,
    val downgradedBy: UUID,
    val applyImmediately: Boolean = false // Usually apply at end of billing period
) {
    init {
        require(newPlan != TenantSubscriptionPlan.ENTERPRISE) {
            "Cannot downgrade to ENTERPRISE plan"
        }
    }
}
