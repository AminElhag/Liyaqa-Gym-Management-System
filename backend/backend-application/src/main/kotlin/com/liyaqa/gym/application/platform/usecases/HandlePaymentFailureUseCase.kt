package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.application.platform.commands.SuspendTenantCommand
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.TenantPaymentFailedEvent
import com.liyaqa.gym.domain.exceptions.NoActiveSubscriptionException
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.domain.services.EmailService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Use case for handling payment failures.
 * Implements escalation strategy: email -> urgent email -> suspension.
 */
@Service
class HandlePaymentFailureUseCase(
    private val tenantRepository: TenantRepository,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val emailService: EmailService,
    private val suspendTenantUseCase: SuspendTenantUseCase,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(HandlePaymentFailureUseCase::class.java)

    suspend fun execute(tenantId: UUID, reason: String): Result<Unit> {
        return try {
            logger.info("Handling payment failure for tenant: $tenantId, reason: $reason")

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

            // Record payment failure
            val updatedSubscription = subscription.recordPaymentFailure(reason)
            subscriptionRepository.save(updatedSubscription).getOrThrow()

            // Handle based on failure count
            when (updatedSubscription.paymentFailureCount) {
                1 -> {
                    // First failure: Send email, retry in 3 days
                    logger.info("First payment failure for tenant $tenantId, scheduling retry in 3 days")

                    emailService.sendPaymentFailureNotice(
                        tenant = tenant,
                        reason = reason,
                        retryDate = LocalDate.now().plusDays(3)
                    ).getOrThrow()

                    // Update next billing date to retry in 3 days
                    val retrySubscription = updatedSubscription.updateNextBillingDate(
                        LocalDate.now().plusDays(3)
                    )
                    subscriptionRepository.save(retrySubscription).getOrThrow()
                }

                2 -> {
                    // Second failure: Send urgent email, retry in 2 days
                    logger.warn("Second payment failure for tenant $tenantId, scheduling retry in 2 days")

                    emailService.sendUrgentPaymentNotice(
                        tenant = tenant,
                        reason = reason,
                        retryDate = LocalDate.now().plusDays(2)
                    ).getOrThrow()

                    // Update next billing date to retry in 2 days
                    val retrySubscription = updatedSubscription.updateNextBillingDate(
                        LocalDate.now().plusDays(2)
                    )
                    subscriptionRepository.save(retrySubscription).getOrThrow()
                }

                else -> {
                    // Third or more failures: Suspend tenant, send final notice
                    logger.error("Third payment failure for tenant $tenantId, suspending account")

                    suspendTenantUseCase.execute(
                        SuspendTenantCommand(
                            tenantId = tenantId,
                            reason = "Payment failure after ${updatedSubscription.paymentFailureCount} attempts"
                        )
                    ).getOrThrow()

                    emailService.sendAccountSuspensionNotice(
                        tenant = tenant,
                        reason = "Payment failed after ${updatedSubscription.paymentFailureCount} attempts"
                    ).getOrThrow()
                }
            }

            // Publish payment failed event
            eventPublisher.publish(
                TenantPaymentFailedEvent(
                    tenantId = tenantId,
                    failureCount = updatedSubscription.paymentFailureCount,
                    reason = reason,
                    occurredAt = Instant.now()
                )
            )

            logger.info("Payment failure handled successfully for tenant $tenantId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to handle payment failure for tenant $tenantId", e)
            Result.failure(e)
        }
    }
}
