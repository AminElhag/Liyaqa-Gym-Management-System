package com.liyaqa.gym.application.onboarding

import com.liyaqa.gym.domain.entities.tenant.OnboardingStatus
import com.liyaqa.gym.domain.entities.tenant.OnboardingSteps
import com.liyaqa.gym.domain.entities.tenant.TenantOnboardingProgress
import com.liyaqa.gym.domain.entities.tenant.OnboardingProgressSummary
import com.liyaqa.gym.domain.entities.tenant.TenantStatus
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.OnboardingStepCompletedEvent
import com.liyaqa.gym.domain.events.TenantActivatedEvent
import com.liyaqa.gym.domain.events.TenantOnboardingCompletedEvent
import com.liyaqa.gym.domain.repositories.TenantOnboardingRepository
import com.liyaqa.gym.domain.repositories.TenantRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Service for managing tenant onboarding process.
 * Handles step initialization, completion tracking, and tenant activation.
 */
@Service
@Transactional
class TenantOnboardingService(
    private val onboardingRepository: TenantOnboardingRepository,
    private val tenantRepository: TenantRepository,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(TenantOnboardingService::class.java)

    /**
     * Initialize onboarding steps for a new tenant
     */
    suspend fun initializeOnboarding(tenantId: UUID) {
        logger.info("Initializing onboarding for tenant: $tenantId")

        val steps = OnboardingSteps.getAllSteps()
        val progressList = steps.map { step ->
            TenantOnboardingProgress.create(
                tenantId = tenantId,
                step = step.key,
                status = if (step.order == 0) OnboardingStatus.PENDING else OnboardingStatus.NOT_STARTED
            )
        }

        onboardingRepository.saveAll(progressList)
        logger.info("Initialized ${progressList.size} onboarding steps for tenant: $tenantId")
    }

    /**
     * Complete an onboarding step
     */
    suspend fun completeStep(
        tenantId: UUID,
        step: String,
        data: Map<String, Any>? = null
    ): Result<OnboardingProgressSummary> {
        return runCatching {
            logger.info("Completing onboarding step: $step for tenant: $tenantId")

            // Find the step
            val progress = onboardingRepository.findByTenantAndStep(tenantId, step)
                ?: throw IllegalArgumentException("Onboarding step not found: $step")

            // Mark as completed
            val updated = progress.complete(data)
            onboardingRepository.save(updated)

            // Publish event
            eventPublisher.publish(
                OnboardingStepCompletedEvent(
                    tenantId = tenantId,
                    step = step,
                    data = data,
                    occurredAt = Instant.now()
                )
            )

            // Unlock next step
            unlockNextStep(tenantId, step)

            // Check if all steps are complete
            if (isOnboardingComplete(tenantId)) {
                handleOnboardingComplete(tenantId)
            }

            // Return progress summary
            getOnboardingProgress(tenantId)
        }
    }

    /**
     * Skip an optional onboarding step
     */
    suspend fun skipStep(
        tenantId: UUID,
        step: String
    ): Result<OnboardingProgressSummary> {
        return runCatching {
            logger.info("Skipping onboarding step: $step for tenant: $tenantId")

            val progress = onboardingRepository.findByTenantAndStep(tenantId, step)
                ?: throw IllegalArgumentException("Onboarding step not found: $step")

            val updated = progress.skip()
            onboardingRepository.save(updated)

            // Unlock next step
            unlockNextStep(tenantId, step)

            // Check if all steps are complete
            if (isOnboardingComplete(tenantId)) {
                handleOnboardingComplete(tenantId)
            }

            getOnboardingProgress(tenantId)
        }
    }

    /**
     * Get onboarding progress summary for a tenant
     */
    suspend fun getOnboardingProgress(tenantId: UUID): OnboardingProgressSummary {
        logger.debug("Getting onboarding progress for tenant: $tenantId")

        val steps = onboardingRepository.findByTenant(tenantId)
        val completed = steps.count { it.status == OnboardingStatus.COMPLETED }
        val total = steps.size

        return OnboardingProgressSummary(
            tenantId = tenantId,
            steps = steps,
            completedSteps = completed,
            totalSteps = total,
            percentComplete = if (total > 0) (completed * 100 / total) else 0,
            currentStep = steps.firstOrNull { it.status == OnboardingStatus.PENDING },
            isComplete = completed == total
        )
    }

    /**
     * Unlock the next onboarding step
     */
    private suspend fun unlockNextStep(tenantId: UUID, completedStep: String) {
        val completedOrder = OnboardingSteps.getStepOrder(completedStep)
        val allSteps = onboardingRepository.findByTenant(tenantId)

        val nextStep = allSteps.firstOrNull { progress ->
            OnboardingSteps.getStepOrder(progress.step) == completedOrder + 1
        }

        if (nextStep != null && nextStep.status == OnboardingStatus.NOT_STARTED) {
            val updated = nextStep.markPending()
            onboardingRepository.save(updated)
            logger.debug("Unlocked next step: ${nextStep.step} for tenant: $tenantId")
        }
    }

    /**
     * Check if onboarding is complete (all steps completed)
     */
    private suspend fun isOnboardingComplete(tenantId: UUID): Boolean {
        return onboardingRepository.isOnboardingComplete(tenantId)
    }

    /**
     * Handle onboarding completion - activate tenant
     */
    private suspend fun handleOnboardingComplete(tenantId: UUID) {
        logger.info("Onboarding completed for tenant: $tenantId")

        // Publish onboarding completed event
        eventPublisher.publish(
            TenantOnboardingCompletedEvent(
                tenantId = tenantId,
                occurredAt = Instant.now()
            )
        )

        // Activate tenant if in trial status
        activateTenant(tenantId)
    }

    /**
     * Activate tenant after onboarding completion
     */
    private suspend fun activateTenant(tenantId: UUID) {
        val tenantResult = tenantRepository.findById(tenantId)
        val tenant = tenantResult.getOrNull()?.orElse(null)

        if (tenant != null && tenant.status == TenantStatus.TRIAL) {
            val activatedTenant = tenant.activate()
            tenantRepository.save(activatedTenant)

            eventPublisher.publish(
                TenantActivatedEvent(
                    tenantId = tenantId,
                    occurredAt = Instant.now()
                )
            )

            logger.info("Tenant activated: $tenantId")
        }
    }
}
