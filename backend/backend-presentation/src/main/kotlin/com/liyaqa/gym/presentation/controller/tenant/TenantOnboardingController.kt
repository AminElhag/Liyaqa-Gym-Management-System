package com.liyaqa.gym.presentation.controller.tenant

import com.liyaqa.gym.application.onboarding.TenantOnboardingService
import com.liyaqa.gym.application.onboarding.commands.CompleteFirstBranchCommand
import com.liyaqa.gym.application.onboarding.commands.CompleteOrganizationSetupCommand
import com.liyaqa.gym.application.onboarding.usecases.CompleteFirstBranchUseCase
import com.liyaqa.gym.application.onboarding.usecases.CompleteOrganizationSetupUseCase
import com.liyaqa.gym.domain.entities.tenant.OnboardingSteps
import com.liyaqa.gym.presentation.dto.onboarding.*
import com.liyaqa.gym.presentation.security.TenantContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Tenant Onboarding Controller
 * Handles tenant user onboarding flow
 */
@RestController
@RequestMapping("/api/v1/tenant/onboarding")
@PreAuthorize("hasAnyRole('TENANT_OWNER', 'TENANT_ADMIN')")
@Tag(name = "Tenant Onboarding", description = "Tenant onboarding endpoints for new tenant setup")
class TenantOnboardingController(
    private val onboardingService: TenantOnboardingService,
    private val completeOrganizationSetupUseCase: CompleteOrganizationSetupUseCase,
    private val completeFirstBranchUseCase: CompleteFirstBranchUseCase
) {

    private val logger = LoggerFactory.getLogger(TenantOnboardingController::class.java)

    /**
     * Get onboarding progress for current tenant
     */
    @GetMapping("/progress")
    @Operation(summary = "Get onboarding progress", description = "Get current tenant's onboarding progress")
    fun getProgress(): ResponseEntity<OnboardingProgressResponse> {
        val tenantId = TenantContext.getCurrentTenantId()
        logger.info("Getting onboarding progress for tenant: $tenantId")

        val progress = runBlocking {
            onboardingService.getOnboardingProgress(tenantId)
        }

        return ResponseEntity.ok(progress.toResponse())
    }

    /**
     * Complete organization setup step
     */
    @PostMapping("/steps/organization")
    @Operation(summary = "Complete organization setup", description = "Complete organization setup step during onboarding")
    fun completeOrganizationSetup(
        @RequestBody @Valid request: OrganizationSetupRequest
    ): ResponseEntity<OnboardingProgressResponse> {
        val tenantId = TenantContext.getCurrentTenantId()
        logger.info("Completing organization setup for tenant: $tenantId")

        val command = CompleteOrganizationSetupCommand(
            tenantId = tenantId,
            organizationName = request.organizationName,
            timezone = request.timezone,
            defaultCurrency = request.defaultCurrency,
            defaultLanguage = request.defaultLanguage
        )

        val organizationId = runBlocking {
            completeOrganizationSetupUseCase.execute(command).getOrThrow()
        }

        logger.info("Organization setup completed. Organization ID: $organizationId")

        // Get updated progress
        val progress = runBlocking {
            onboardingService.getOnboardingProgress(tenantId)
        }

        return ResponseEntity.ok(progress.toResponse())
    }

    /**
     * Complete first branch setup step
     */
    @PostMapping("/steps/first-branch")
    @Operation(summary = "Complete first branch setup", description = "Create first branch during onboarding")
    fun completeFirstBranch(
        @RequestBody @Valid request: FirstBranchRequest,
        @RequestParam organizationId: UUID
    ): ResponseEntity<OnboardingProgressResponse> {
        val tenantId = TenantContext.getCurrentTenantId()
        logger.info("Creating first branch for tenant: $tenantId")

        val command = CompleteFirstBranchCommand(
            tenantId = tenantId,
            organizationId = organizationId,
            branchName = request.branchName,
            address = request.address.toDomain(),
            facilityType = request.facilityType
        )

        val branchId = runBlocking {
            completeFirstBranchUseCase.execute(command).getOrThrow()
        }

        logger.info("First branch created. Branch ID: $branchId")

        // Get updated progress
        val progress = runBlocking {
            onboardingService.getOnboardingProgress(tenantId)
        }

        return ResponseEntity.ok(progress.toResponse())
    }

    /**
     * Complete membership plans setup step
     */
    @PostMapping("/steps/membership-plans")
    @Operation(summary = "Complete membership plans", description = "Create initial membership plans")
    fun completeMembershipPlans(
        @RequestBody @Valid request: CreateInitialPlansRequest
    ): ResponseEntity<OnboardingProgressResponse> {
        val tenantId = TenantContext.getCurrentTenantId()
        logger.info("Creating membership plans for tenant: $tenantId")

        // TODO: Implement membership plan creation
        // For now, just mark the step as complete
        val planIds = request.plans.map { UUID.randomUUID() }

        runBlocking {
            onboardingService.completeStep(
                tenantId = tenantId,
                step = OnboardingSteps.MEMBERSHIP_PLANS,
                data = mapOf("planIds" to planIds.map { it.toString() })
            ).getOrThrow()
        }

        logger.info("Membership plans setup completed")

        // Get updated progress
        val progress = runBlocking {
            onboardingService.getOnboardingProgress(tenantId)
        }

        return ResponseEntity.ok(progress.toResponse())
    }

    /**
     * Complete staff invitation step
     */
    @PostMapping("/steps/staff-invited")
    @Operation(summary = "Complete staff invitation", description = "Invite staff members during onboarding")
    fun completeStaffInvitation(
        @RequestBody @Valid request: StaffInvitationRequest
    ): ResponseEntity<OnboardingProgressResponse> {
        val tenantId = TenantContext.getCurrentTenantId()
        logger.info("Inviting staff for tenant: $tenantId")

        // TODO: Implement staff invitation
        // For now, just mark the step as complete
        val invitationIds = request.invitations.map { UUID.randomUUID() }

        runBlocking {
            onboardingService.completeStep(
                tenantId = tenantId,
                step = OnboardingSteps.STAFF_INVITED,
                data = mapOf("invitationIds" to invitationIds.map { it.toString() })
            ).getOrThrow()
        }

        logger.info("Staff invitation completed")

        // Get updated progress
        val progress = runBlocking {
            onboardingService.getOnboardingProgress(tenantId)
        }

        return ResponseEntity.ok(progress.toResponse())
    }

    /**
     * Complete payment setup step
     */
    @PostMapping("/steps/payment-setup")
    @Operation(summary = "Complete payment setup", description = "Configure payment methods")
    fun completePaymentSetup(
        @RequestBody @Valid request: PaymentSetupRequest
    ): ResponseEntity<OnboardingProgressResponse> {
        val tenantId = TenantContext.getCurrentTenantId()
        logger.info("Setting up payment for tenant: $tenantId")

        // TODO: Implement payment gateway configuration
        // For now, just mark the step as complete
        runBlocking {
            onboardingService.completeStep(
                tenantId = tenantId,
                step = OnboardingSteps.PAYMENT_SETUP,
                data = mapOf("paymentGateway" to request.paymentGateway)
            ).getOrThrow()
        }

        logger.info("Payment setup completed")

        // Get updated progress
        val progress = runBlocking {
            onboardingService.getOnboardingProgress(tenantId)
        }

        return ResponseEntity.ok(progress.toResponse())
    }

    /**
     * Skip an optional onboarding step
     */
    @PostMapping("/skip/{step}")
    @Operation(summary = "Skip step", description = "Skip an optional onboarding step")
    fun skipStep(
        @PathVariable step: String
    ): ResponseEntity<OnboardingProgressResponse> {
        val tenantId = TenantContext.getCurrentTenantId()
        logger.info("Skipping step: $step for tenant: $tenantId")

        val progress = runBlocking {
            onboardingService.skipStep(tenantId, step).getOrThrow()
        }

        return ResponseEntity.ok(progress.toResponse())
    }

    /**
     * Complete the entire onboarding process
     */
    @PostMapping("/complete")
    @Operation(summary = "Complete onboarding", description = "Mark onboarding as complete")
    fun completeOnboarding(): ResponseEntity<OnboardingProgressResponse> {
        val tenantId = TenantContext.getCurrentTenantId()
        logger.info("Completing onboarding for tenant: $tenantId")

        val progress = runBlocking {
            onboardingService.completeStep(
                tenantId = tenantId,
                step = OnboardingSteps.COMPLETE,
                data = null
            ).getOrThrow()
        }

        return ResponseEntity.ok(progress.toResponse())
    }
}
