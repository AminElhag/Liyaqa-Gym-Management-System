package com.liyaqa.gym.application.onboarding.usecases

import com.liyaqa.gym.application.onboarding.TenantOnboardingService
import com.liyaqa.gym.application.onboarding.commands.CompleteOrganizationSetupCommand
import com.liyaqa.gym.domain.entities.Organization
import com.liyaqa.gym.domain.entities.OrganizationSettings
import com.liyaqa.gym.domain.entities.tenant.OnboardingSteps
import com.liyaqa.gym.domain.repositories.OrganizationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.util.UUID

/**
 * Use case for completing organization setup during onboarding
 */
@Service
@Transactional
class CompleteOrganizationSetupUseCase(
    private val organizationRepository: OrganizationRepository,
    private val onboardingService: TenantOnboardingService
) {

    private val logger = LoggerFactory.getLogger(CompleteOrganizationSetupUseCase::class.java)

    suspend fun execute(command: CompleteOrganizationSetupCommand): Result<UUID> {
        return runCatching {
            logger.info("Completing organization setup for tenant: ${command.tenantId}")

            // Create organization settings
            val settings = OrganizationSettings.default().copy(
                defaultCurrency = command.defaultCurrency,
                defaultLanguage = command.defaultLanguage
            )

            // Create organization
            val organization = Organization.create(
                tenantId = command.tenantId,
                name = command.organizationName,
                timezone = ZoneId.of(command.timezone),
                settings = settings
            )

            // Save organization
            val savedOrg = organizationRepository.save(organization)
                .getOrElse { error ->
                    logger.error("Failed to save organization", error)
                    throw error
                }

            logger.info("Organization created: ${savedOrg.id}")

            // Complete onboarding step
            onboardingService.completeStep(
                tenantId = command.tenantId,
                step = OnboardingSteps.ORGANIZATION_SETUP,
                data = mapOf("organizationId" to savedOrg.id.toString())
            ).getOrThrow()

            savedOrg.id
        }
    }
}
