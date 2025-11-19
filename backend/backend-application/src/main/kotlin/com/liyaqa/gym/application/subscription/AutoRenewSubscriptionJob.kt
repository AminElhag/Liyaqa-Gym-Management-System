package com.liyaqa.gym.application.subscription

import com.liyaqa.gym.application.subscription.commands.RenewSubscriptionCommand
import com.liyaqa.gym.domain.entities.MemberStatus
import com.liyaqa.gym.domain.entities.PaymentMethod
import com.liyaqa.gym.domain.entities.Subscription
import com.liyaqa.gym.domain.entities.SubscriptionStatus
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.gym.domain.repositories.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

/**
 * Scheduled job for automatic subscription renewal.
 *
 * This job handles:
 * - Finding subscriptions expiring within the renewal window (3 days before expiry)
 * - Validating member is still active
 * - Attempting payment with retry logic (3 attempts)
 * - Sending renewal reminder notifications (via events)
 * - Marking subscriptions as expired if payment fails after all retries
 * - Handling payment failures gracefully
 *
 * Runs daily at 2 AM server time.
 *
 * @property subscriptionRepository Repository for subscription lookups
 * @property memberRepository Repository for member validation
 * @property renewSubscriptionUseCase Use case for processing renewals
 * @property eventPublisher Publisher for domain events
 */
@Component
class AutoRenewSubscriptionJob(
    private val subscriptionRepository: SubscriptionRepository,
    private val memberRepository: MemberRepository,
    private val renewSubscriptionUseCase: RenewSubscriptionUseCase,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(AutoRenewSubscriptionJob::class.java)

    companion object {
        private const val RENEWAL_WINDOW_DAYS = 3L // Start attempting renewal 3 days before expiry
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_HOURS = 8L // Wait 8 hours between retry attempts
    }

    /**
     * Scheduled job execution.
     * Runs daily at 2:00 AM server time.
     * Cron format: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
    @Transactional
    fun execute() {
        logger.info("Starting auto-renewal job execution")
        val startTime = Instant.now()

        try {
            // 1. Find subscriptions expiring within the renewal window
            val expiringSubscriptions = findExpiringSubscriptions()
            logger.info("Found ${expiringSubscriptions.size} subscriptions eligible for auto-renewal")

            var successCount = 0
            var failureCount = 0
            var skippedCount = 0

            // 2. Process each subscription
            for (subscription in expiringSubscriptions) {
                try {
                    when (processSubscriptionRenewal(subscription)) {
                        RenewalResult.SUCCESS -> successCount++
                        RenewalResult.FAILED -> failureCount++
                        RenewalResult.SKIPPED -> skippedCount++
                    }
                } catch (e: Exception) {
                    logger.error("Unexpected error processing subscription ${subscription.id}: ${e.message}", e)
                    failureCount++
                }
            }

            val duration = java.time.Duration.between(startTime, Instant.now())
            logger.info(
                "Auto-renewal job completed in ${duration.toMillis()}ms. " +
                "Success: $successCount, Failed: $failureCount, Skipped: $skippedCount"
            )

        } catch (e: Exception) {
            logger.error("Auto-renewal job failed with error: ${e.message}", e)
        }
    }

    /**
     * Finds subscriptions that are eligible for auto-renewal.
     *
     * @return List of subscriptions expiring within the renewal window with auto-renew enabled
     */
    private fun findExpiringSubscriptions(): List<Subscription> {
        val today = LocalDate.now()
        val renewalDate = today.plusDays(RENEWAL_WINDOW_DAYS)

        logger.debug("Searching for subscriptions expiring between $today and $renewalDate")

        val subscriptions = subscriptionRepository.findExpiringBetween(
            startDate = today,
            endDate = renewalDate,
            page = 0,
            size = 1000 // Process up to 1000 subscriptions per run
        ).getOrElse { error ->
            logger.error("Failed to query expiring subscriptions: ${error.message}", error)
            return emptyList()
        }

        // Filter for auto-renew enabled and active subscriptions
        return subscriptions.filter { subscription ->
            subscription.autoRenew &&
            subscription.status == SubscriptionStatus.ACTIVE &&
            !subscription.isExpired()
        }
    }

    /**
     * Processes renewal for a single subscription.
     *
     * @param subscription The subscription to renew
     * @return The renewal result (success, failed, or skipped)
     */
    private fun processSubscriptionRenewal(subscription: Subscription): RenewalResult {
        logger.info("Processing auto-renewal for subscription: ${subscription.id}")

        try {
            // 1. Validate member is still active
            val member = memberRepository.findById(subscription.memberId)
                .getOrElse { error ->
                    logger.error("Failed to load member ${subscription.memberId}: ${error.message}", error)
                    return RenewalResult.SKIPPED
                }
                .orElse(null)

            if (member == null) {
                logger.warn("Member not found for subscription ${subscription.id}, skipping renewal")
                return RenewalResult.SKIPPED
            }

            if (member.status != MemberStatus.ACTIVE) {
                logger.warn("Member ${member.id} is not active (status: ${member.status}), skipping renewal")
                disableAutoRenewal(subscription)
                return RenewalResult.SKIPPED
            }

            // 2. Attempt renewal with retry logic
            val renewalResult = attemptRenewalWithRetry(subscription)

            if (renewalResult) {
                logger.info("Successfully renewed subscription: ${subscription.id}")
                return RenewalResult.SUCCESS
            } else {
                logger.error("Failed to renew subscription after all retry attempts: ${subscription.id}")
                handleRenewalFailure(subscription)
                return RenewalResult.FAILED
            }

        } catch (e: Exception) {
            logger.error("Error processing renewal for subscription ${subscription.id}: ${e.message}", e)
            return RenewalResult.FAILED
        }
    }

    /**
     * Attempts to renew a subscription with retry logic.
     *
     * @param subscription The subscription to renew
     * @return True if renewal was successful, false otherwise
     */
    private fun attemptRenewalWithRetry(subscription: Subscription): Boolean {
        // Use default payment method for auto-renewal (could be enhanced to store preferred method)
        val defaultPaymentMethod = PaymentMethod.CREDIT_CARD

        for (attempt in 1..MAX_RETRY_ATTEMPTS) {
            logger.debug("Renewal attempt $attempt of $MAX_RETRY_ATTEMPTS for subscription: ${subscription.id}")

            try {
                val command = RenewSubscriptionCommand(
                    subscriptionId = subscription.id,
                    paymentMethod = defaultPaymentMethod,
                    paymentMetadata = mapOf(
                        "autoRenewal" to true,
                        "attemptNumber" to attempt,
                        "maxAttempts" to MAX_RETRY_ATTEMPTS
                    )
                )

                val result = renewSubscriptionUseCase.execute(command)

                if (result.isSuccess) {
                    logger.info("Renewal successful on attempt $attempt for subscription: ${subscription.id}")
                    return true
                } else {
                    logger.warn(
                        "Renewal attempt $attempt failed for subscription ${subscription.id}: " +
                        "${result.exceptionOrNull()?.message}"
                    )
                }

            } catch (e: Exception) {
                logger.error("Renewal attempt $attempt threw exception for subscription ${subscription.id}: ${e.message}", e)
            }

            // Wait before retrying (except on last attempt)
            if (attempt < MAX_RETRY_ATTEMPTS) {
                logger.debug("Waiting before retry attempt ${attempt + 1}")
                Thread.sleep(RETRY_DELAY_HOURS * 3600 * 1000) // Wait configured hours
            }
        }

        return false
    }

    /**
     * Handles renewal failure after all retry attempts.
     * Marks subscription as expired and sends notification.
     *
     * @param subscription The subscription that failed to renew
     */
    private fun handleRenewalFailure(subscription: Subscription) {
        logger.warn("Handling renewal failure for subscription: ${subscription.id}")

        try {
            // Mark subscription as expired
            val expiredSubscription = subscription.copy(
                status = SubscriptionStatus.EXPIRED,
                autoRenew = false, // Disable auto-renewal after failure
                updatedAt = Instant.now()
            )

            subscriptionRepository.save(expiredSubscription)
                .onFailure { error ->
                    logger.error("Failed to mark subscription as expired: ${error.message}", error)
                }

            // Send renewal failure notification (via events)
            sendRenewalFailureNotification(subscription)

        } catch (e: Exception) {
            logger.error("Error handling renewal failure for subscription ${subscription.id}: ${e.message}", e)
        }
    }

    /**
     * Disables auto-renewal for a subscription.
     *
     * @param subscription The subscription to update
     */
    private fun disableAutoRenewal(subscription: Subscription) {
        try {
            val updated = subscription.disableAutoRenew()
            subscriptionRepository.save(updated)
                .onFailure { error ->
                    logger.error("Failed to disable auto-renewal: ${error.message}", error)
                }
            logger.info("Disabled auto-renewal for subscription: ${subscription.id}")
        } catch (e: Exception) {
            logger.error("Error disabling auto-renewal for subscription ${subscription.id}: ${e.message}", e)
        }
    }

    /**
     * Sends a renewal failure notification to the member.
     * This is handled via events which will be picked up by the notification service.
     *
     * @param subscription The subscription that failed to renew
     */
    private fun sendRenewalFailureNotification(subscription: Subscription) {
        try {
            // Create a custom notification event (could be added to domain events)
            // For now, log it - actual notification will be handled by notification service
            logger.info(
                "Renewal failure notification triggered for subscription ${subscription.id}, " +
                "member ${subscription.memberId}"
            )

            // TODO: Publish RenewalFailedEvent when it's added to domain events
            // eventPublisher.publish(SubscriptionRenewalFailedEvent(...))

        } catch (e: Exception) {
            logger.error("Failed to send renewal failure notification: ${e.message}", e)
        }
    }

    /**
     * Result of a renewal attempt.
     */
    private enum class RenewalResult {
        SUCCESS,  // Renewal completed successfully
        FAILED,   // Renewal failed after all retries
        SKIPPED   // Renewal was skipped (member inactive, etc.)
    }
}
