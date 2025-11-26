package com.liyaqa.gym.application.onboarding.usecases

import com.liyaqa.gym.application.onboarding.TenantOnboardingService
import com.liyaqa.gym.application.onboarding.commands.CompleteFirstBranchCommand
import com.liyaqa.gym.domain.entities.Branch
import com.liyaqa.gym.domain.entities.FacilityType
import com.liyaqa.gym.domain.entities.tenant.OnboardingSteps
import com.liyaqa.gym.domain.repositories.BranchRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Use case for completing first branch setup during onboarding
 */
@Service
@Transactional
class CompleteFirstBranchUseCase(
    private val branchRepository: BranchRepository,
    private val onboardingService: TenantOnboardingService
) {

    private val logger = LoggerFactory.getLogger(CompleteFirstBranchUseCase::class.java)

    suspend fun execute(command: CompleteFirstBranchCommand): Result<UUID> {
        return runCatching {
            logger.info("Creating first branch for tenant: ${command.tenantId}")

            // Parse facility type
            val facilityType = FacilityType.valueOf(command.facilityType)

            // Create branch
            val branch = Branch.create(
                tenantId = command.tenantId,
                organizationId = command.organizationId,
                name = command.branchName,
                address = command.address,
                facilityType = facilityType
            )

            // Save branch
            val savedBranch = branchRepository.save(branch)
                .getOrElse { error ->
                    logger.error("Failed to save branch", error)
                    throw error
                }

            logger.info("Branch created: ${savedBranch.id}")

            // Complete onboarding step
            onboardingService.completeStep(
                tenantId = command.tenantId,
                step = OnboardingSteps.FIRST_BRANCH,
                data = mapOf("branchId" to savedBranch.id.toString())
            ).getOrThrow()

            savedBranch.id
        }
    }
}
