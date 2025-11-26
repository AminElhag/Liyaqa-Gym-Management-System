package com.liyaqa.gym.presentation.dto.onboarding

import com.liyaqa.gym.domain.entities.tenant.OnboardingStatus
import com.liyaqa.gym.domain.entities.tenant.TenantOnboardingProgress
import com.liyaqa.gym.domain.entities.tenant.OnboardingProgressSummary
import com.liyaqa.gym.presentation.dto.platform.AddressRequest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

/**
 * Response DTO for onboarding progress
 */
data class OnboardingProgressResponse(
    val tenantId: UUID,
    val steps: List<OnboardingStepResponse>,
    val completedSteps: Int,
    val totalSteps: Int,
    val percentComplete: Int,
    val currentStep: OnboardingStepResponse?,
    val isComplete: Boolean
)

/**
 * Response DTO for individual onboarding step
 */
data class OnboardingStepResponse(
    val id: UUID,
    val step: String,
    val status: OnboardingStatus,
    val completedAt: Instant?,
    val data: Map<String, Any>?
)

/**
 * Request DTO for organization setup
 */
data class OrganizationSetupRequest(
    @field:NotBlank(message = "Organization name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val organizationName: String,

    @field:NotBlank(message = "Timezone is required")
    val timezone: String = "Asia/Riyadh",

    @field:NotBlank(message = "Default currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters")
    val defaultCurrency: String = "SAR",

    @field:NotBlank(message = "Default language is required")
    @field:Pattern(regexp = "^(ar|en)$", message = "Language must be 'ar' or 'en'")
    val defaultLanguage: String = "ar"
)

/**
 * Request DTO for first branch setup
 */
data class FirstBranchRequest(
    @field:NotBlank(message = "Branch name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val branchName: String,

    val address: AddressRequest,

    @field:NotBlank(message = "Facility type is required")
    @field:Pattern(regexp = "^(MALE_ONLY|FEMALE_ONLY|FAMILY)$", message = "Invalid facility type")
    val facilityType: String
)

/**
 * Request DTO for creating initial membership plans
 */
data class CreateInitialPlansRequest(
    val plans: List<MembershipPlanRequest>
)

/**
 * Request DTO for membership plan
 */
data class MembershipPlanRequest(
    @field:NotBlank(message = "Plan name is required")
    val name: String,

    @field:NotBlank(message = "Arabic name is required")
    val nameArabic: String,

    val durationMonths: Int,

    val price: Double,

    val description: String?
)

/**
 * Request DTO for staff invitation
 */
data class StaffInvitationRequest(
    val invitations: List<StaffInviteData>
)

data class StaffInviteData(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Role is required")
    val role: String
)

/**
 * Request DTO for payment setup
 */
data class PaymentSetupRequest(
    @field:NotBlank(message = "Payment gateway is required")
    val paymentGateway: String,

    val apiKey: String?,

    val merchantId: String?
)

/**
 * Extension functions for mapping
 */
fun OnboardingProgressSummary.toResponse(): OnboardingProgressResponse {
    return OnboardingProgressResponse(
        tenantId = tenantId,
        steps = steps.map { it.toResponse() },
        completedSteps = completedSteps,
        totalSteps = totalSteps,
        percentComplete = percentComplete,
        currentStep = currentStep?.toResponse(),
        isComplete = isComplete
    )
}

fun TenantOnboardingProgress.toResponse(): OnboardingStepResponse {
    return OnboardingStepResponse(
        id = id,
        step = step,
        status = status,
        completedAt = completedAt,
        data = data
    )
}
