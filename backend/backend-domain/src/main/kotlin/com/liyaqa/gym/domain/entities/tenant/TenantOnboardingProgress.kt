package com.liyaqa.gym.domain.entities.tenant

import java.time.Instant
import java.util.UUID

/**
 * Tenant onboarding progress tracking entity
 * Tracks individual step completion for new tenant onboarding
 */
data class TenantOnboardingProgress(
    val id: UUID,
    val tenantId: UUID,
    val step: String, // ACCOUNT_VERIFIED, ORGANIZATION_SETUP, etc.
    val status: OnboardingStatus,
    val completedAt: Instant?,
    val data: Map<String, Any>?, // Flexible storage for step-specific data
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        require(step.isNotBlank()) { "Onboarding step cannot be blank" }
    }

    fun complete(data: Map<String, Any>? = null): TenantOnboardingProgress {
        return copy(
            status = OnboardingStatus.COMPLETED,
            completedAt = Instant.now(),
            data = data,
            updatedAt = Instant.now()
        )
    }

    fun markPending(): TenantOnboardingProgress {
        return copy(
            status = OnboardingStatus.PENDING,
            updatedAt = Instant.now()
        )
    }

    fun skip(): TenantOnboardingProgress {
        return copy(
            status = OnboardingStatus.COMPLETED,
            completedAt = Instant.now(),
            data = mapOf("skipped" to true),
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun create(
            tenantId: UUID,
            step: String,
            status: OnboardingStatus = OnboardingStatus.NOT_STARTED
        ): TenantOnboardingProgress {
            val now = Instant.now()
            return TenantOnboardingProgress(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                step = step,
                status = status,
                completedAt = null,
                data = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Onboarding status for each step
 */
enum class OnboardingStatus {
    NOT_STARTED,  // Step is locked and not yet available
    PENDING,      // Step is available for completion
    COMPLETED     // Step has been completed
}

/**
 * Onboarding step information
 */
data class OnboardingStep(
    val key: String,
    val title: String,
    val description: String,
    val order: Int,
    val isRequired: Boolean = true
)

/**
 * Complete onboarding progress summary
 */
data class OnboardingProgressSummary(
    val tenantId: UUID,
    val steps: List<TenantOnboardingProgress>,
    val completedSteps: Int,
    val totalSteps: Int,
    val percentComplete: Int,
    val currentStep: TenantOnboardingProgress?,
    val isComplete: Boolean
)

/**
 * Onboarding step keys
 */
object OnboardingSteps {
    const val ACCOUNT_VERIFIED = "ACCOUNT_VERIFIED"
    const val ORGANIZATION_SETUP = "ORGANIZATION_SETUP"
    const val FIRST_BRANCH = "FIRST_BRANCH"
    const val MEMBERSHIP_PLANS = "MEMBERSHIP_PLANS"
    const val STAFF_INVITED = "STAFF_INVITED"
    const val PAYMENT_SETUP = "PAYMENT_SETUP"
    const val COMPLETE = "COMPLETE"

    /**
     * Get all onboarding steps in order
     */
    fun getAllSteps(): List<OnboardingStep> = listOf(
        OnboardingStep(ACCOUNT_VERIFIED, "Verify Account", "Verify your email address", 0, true),
        OnboardingStep(ORGANIZATION_SETUP, "Setup Organization", "Set up your organization profile", 1, true),
        OnboardingStep(FIRST_BRANCH, "Add Branch", "Add your first branch/location", 2, true),
        OnboardingStep(MEMBERSHIP_PLANS, "Create Plans", "Create membership plans", 3, true),
        OnboardingStep(STAFF_INVITED, "Invite Staff", "Invite staff members", 4, false),
        OnboardingStep(PAYMENT_SETUP, "Payment Setup", "Configure payment methods", 5, false),
        OnboardingStep(COMPLETE, "Complete", "Onboarding complete", 6, true)
    )

    /**
     * Get step order by key
     */
    fun getStepOrder(stepKey: String): Int {
        return getAllSteps().find { it.key == stepKey }?.order ?: -1
    }
}
