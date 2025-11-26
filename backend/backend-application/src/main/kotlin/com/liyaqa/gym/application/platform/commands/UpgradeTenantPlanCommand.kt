package com.liyaqa.gym.application.platform.commands

import com.liyaqa.gym.domain.entities.tenant.TenantSubscriptionPlan
import java.util.UUID

/**
 * Command to upgrade a tenant's subscription plan.
 */
data class UpgradeTenantPlanCommand(
    val tenantId: UUID,
    val newPlan: TenantSubscriptionPlan,
    val upgradedBy: UUID,
    val applyImmediately: Boolean = true
) {
    init {
        require(newPlan != TenantSubscriptionPlan.STARTER) {
            "Cannot upgrade to STARTER plan"
        }
    }
}
