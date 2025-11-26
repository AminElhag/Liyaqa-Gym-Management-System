package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.application.platform.commands.SuspendTenantCommand
import com.liyaqa.gym.domain.entities.tenant.TenantStatus
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.TenantTrialExpiredEvent
import com.liyaqa.gym.domain.exceptions.NoActiveSubscriptionException
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.domain.services.EmailService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

/**
 * Use case for handling trial subscription expirations.
 * Attempts to convert trial to paid subscription or suspends the tenant.
 */
@Service
class HandleTrialExpirationUseCase(
    private val tenantRepository: TenantRepository,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val processBillingUseCase: ProcessTenantBillingUseCase,
    private val suspendTenantUseCase: SuspendTenantUseCase,
    private val emailService: EmailService,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(HandleTrialExpirationUseCase::class.java)

    suspend fun execute(tenantId: UUID): Result<Unit> {
        return try {
            logger.info("Handling trial expiration for tenant: $tenantId")

            // Find tenant
            val tenantOpt = tenantRepository.findById(tenantId).getOrThrow()
            if (!tenantOpt.isPresent) {
                return Result.failure(TenantNotFoundException(tenantId))
            }
            val tenant = tenantOpt.get()

            // Find active subscription
            val subscriptionOpt = subscriptionRepository.findActiveByTenant(tenantId).getOrThrow()
            if (!subscriptionOpt.isPresent) {
                return Result.failure(NoActiveSubscriptionException(tenantId))
            }
            val subscription = subscriptionOpt.get()

            val hadPaymentMethod = subscription.paymentMethod != null
            var wasConverted = false

            // Check if payment method is on file
            if (subscription.paymentMethod != null) {
                logger.info("Payment method found for tenant $tenantId, attempting to charge")

                // Attempt to charge
                val billingResult = processBillingUseCase.execute(tenantId)

                if (billingResult.isSuccess) {
                    // Successfully converted to paid
                    logger.info("Successfully converted trial to paid for tenant $tenantId")

                    val convertedSubscription = subscription.convertFromTrial()
                    subscriptionRepository.save(convertedSubscription).getOrThrow()

                    val updatedTenant = tenant.copy(
                        status = TenantStatus.ACTIVE
                    )
                    tenantRepository.save(updatedTenant).getOrThrow()

                    emailService.sendTrialConvertedNotice(tenant).getOrThrow()
                    wasConverted = true
                } else {
                    // Payment failed
                    logger.warn("Payment failed for trial expiration of tenant $tenantId")

                    suspendTenantUseCase.execute(
                        SuspendTenantCommand(
                            tenantId = tenantId,
                            reason = "Trial expired and payment failed"
                        )
                    ).getOrThrow()

                    emailService.sendTrialExpiredPaymentFailedNotice(tenant).getOrThrow()
                }
            } else {
                // No payment method on file
                logger.warn("No payment method for trial expiration of tenant $tenantId")

                suspendTenantUseCase.execute(
                    SuspendTenantCommand(
                        tenantId = tenantId,
                        reason = "Trial expired without payment method"
                    )
                ).getOrThrow()

                emailService.sendTrialExpiredNoPaymentMethodNotice(tenant).getOrThrow()
            }

            // Publish trial expired event
            eventPublisher.publish(
                TenantTrialExpiredEvent(
                    tenantId = tenantId,
                    hadPaymentMethod = hadPaymentMethod,
                    wasConverted = wasConverted,
                    occurredAt = Instant.now()
                )
            )

            logger.info("Trial expiration handled successfully for tenant $tenantId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to handle trial expiration for tenant $tenantId", e)
            Result.failure(e)
        }
    }
}
