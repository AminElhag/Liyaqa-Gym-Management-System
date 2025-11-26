package com.liyaqa.gym.application.platform.schedulers

import com.liyaqa.gym.application.platform.usecases.ProcessTenantBillingUseCase
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Job for retrying failed payment attempts.
 * Runs daily to retry subscriptions that are scheduled for retry today.
 */
@Component
class RetryFailedPaymentsJob(
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val processBillingUseCase: ProcessTenantBillingUseCase
) {

    private val logger = LoggerFactory.getLogger(RetryFailedPaymentsJob::class.java)

    /**
     * Retry failed payments for subscriptions due today with failure count < 3.
     * Runs daily at 3 AM (after the main billing process).
     */
    @Scheduled(cron = "0 0 3 * * *")
    fun retryFailedPayments() = runBlocking {
        logger.info("Starting failed payments retry process")

        try {
            // Find subscriptions due for retry today
            val subscriptionsToRetry = subscriptionRepository
                .findByNextBillingDate(LocalDate.now())
                .getOrElse {
                    logger.error("Failed to fetch subscriptions for retry", it)
                    emptyList()
                }
                .filter { subscription ->
                    // Only retry if there have been failures but less than 3
                    subscription.paymentFailureCount > 0 && subscription.paymentFailureCount < 3
                }

            logger.info("Found ${subscriptionsToRetry.size} subscriptions to retry")

            subscriptionsToRetry.forEach { subscription ->
                try {
                    logger.info("Retrying payment for tenant ${subscription.tenantId} " +
                            "(attempt ${subscription.paymentFailureCount + 1})")

                    // Attempt billing
                    val billingResult = processBillingUseCase.execute(subscription.tenantId)

                    if (billingResult.isSuccess) {
                        logger.info("Payment retry successful for tenant ${subscription.tenantId}")

                        // Reset failure count on success
                        val resetSubscription = subscription.resetPaymentFailures()
                        subscriptionRepository.save(resetSubscription).getOrThrow()

                        logger.info("Payment failure count reset for tenant ${subscription.tenantId}")
                    } else {
                        // Failure will be handled by HandlePaymentFailureUseCase in the main billing process
                        logger.warn("Payment retry failed for tenant ${subscription.tenantId}")
                    }
                } catch (e: Exception) {
                    logger.error("Failed to retry payment for tenant ${subscription.tenantId}", e)
                }
            }

            logger.info("Failed payments retry process completed")
        } catch (e: Exception) {
            logger.error("Failed payments retry process failed", e)
        }
    }
}
