package com.liyaqa.gym.application.platform.commands

import com.liyaqa.gym.domain.entities.tenant.BillingCycle
import com.liyaqa.gym.domain.entities.tenant.BusinessType
import com.liyaqa.gym.domain.entities.tenant.TenantContactInfo
import com.liyaqa.gym.domain.entities.tenant.TenantSubscriptionPlan
import com.liyaqa.gym.domain.valueobjects.Address
import java.util.UUID

/**
 * Command to create a new tenant on the platform.
 */
data class CreateTenantCommand(
    val name: String,
    val nameArabic: String,
    val slug: String,
    val businessType: BusinessType,
    val plan: TenantSubscriptionPlan,
    val billingCycle: BillingCycle,
    val contactInfo: TenantContactInfo,
    val address: Address,
    val vatNumber: String?,
    val commercialRegistration: String?,
    val ownerName: String,
    val ownerEmail: String,
    val ownerPhone: String,
    val startWithTrial: Boolean = true,
    val createdByAdminId: UUID
) {
    init {
        require(name.isNotBlank()) { "Tenant name cannot be blank" }
        require(nameArabic.isNotBlank()) { "Tenant Arabic name cannot be blank" }
        require(slug.isNotBlank()) { "Tenant slug cannot be blank" }
        require(slug.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$"))) {
            "Slug must be lowercase alphanumeric with hyphens only"
        }
        require(ownerName.isNotBlank()) { "Owner name cannot be blank" }
        require(ownerEmail.isNotBlank()) { "Owner email cannot be blank" }
        require(ownerPhone.isNotBlank()) { "Owner phone cannot be blank" }
        require(ownerEmail.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
            "Invalid owner email format"
        }
    }
}
