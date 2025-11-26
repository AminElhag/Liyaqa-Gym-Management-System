package com.liyaqa.gym.application.onboarding.commands

import com.liyaqa.gym.domain.valueobjects.Address
import java.util.UUID

/**
 * Command for completing organization setup during onboarding
 */
data class CompleteOrganizationSetupCommand(
    val tenantId: UUID,
    val organizationName: String,
    val timezone: String = "Asia/Riyadh",
    val defaultCurrency: String = "SAR",
    val defaultLanguage: String = "ar"
)

/**
 * Command for creating first branch during onboarding
 */
data class CompleteFirstBranchCommand(
    val tenantId: UUID,
    val organizationId: UUID,
    val branchName: String,
    val address: Address,
    val facilityType: String // "MALE_ONLY", "FEMALE_ONLY", "FAMILY"
)

/**
 * Command for creating initial membership plans
 */
data class CreateInitialPlansCommand(
    val tenantId: UUID,
    val organizationId: UUID,
    val plans: List<MembershipPlanData>
)

data class MembershipPlanData(
    val name: String,
    val nameArabic: String,
    val durationMonths: Int,
    val price: Double,
    val description: String?
)
