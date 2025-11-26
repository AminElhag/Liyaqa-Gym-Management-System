package com.liyaqa.gym.application.platform.schedulers

import com.liyaqa.gym.application.platform.usecases.HandlePaymentFailureUseCase
import com.liyaqa.gym.application.platform.usecases.HandleTrialExpirationUseCase
import com.liyaqa.gym.application.platform.usecases.ProcessTenantBillingUseCase
import com.liyaqa.gym.application.platform.usecases.SendInvoiceEmailUseCase
import com.liyaqa.gym.application.platform.usecases.SendPaymentReminderEmailUseCase
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Scheduler for automated tenant billing processes.
 * Handles daily billing, trial expirations, and payment reminders.
 */
@Component
class TenantBillingScheduler(
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val processBillingUseCase: ProcessTenantBillingUseCase,
    private val sendInvoiceEmailUseCase: SendInvoiceEmailUseCase,
    private val handlePaymentFailureUseCase: HandlePaymentFailureUseCase,
    private val handleTrialExpirationUseCase: HandleTrialExpirationUseCase,
    private val sendPaymentReminderEmailUseCase: SendPaymentReminderEmailUseCase
) {

    private val logger = LoggerFactory.getLogger(TenantBillingScheduler::class.java)

    /**
     * Process daily billing for subscriptions due today.
     * Runs every day at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    fun processDailyBilling() = runBlocking {
        logger.info("Starting daily billing process")

        try {
            // Get all subscriptions due for billing today
            val dueSubscriptions = subscriptionRepository
                .findByNextBillingDate(LocalDate.now())
                .getOrElse {
                    logger.error("Failed to fetch subscriptions due for billing", it)
                    emptyList()
                }

            logger.info("Found ${dueSubscriptions.size} subscriptions due for billing")

            dueSubscriptions.forEach { subscription ->
                try {
                    logger.info("Processing billing for tenant ${subscription.tenantId}")

                    // Process billing
                    val invoiceResult = processBillingUseCase.execute(subscription.tenantId)

                    if (invoiceResult.isSuccess) {
                        val invoice = invoiceResult.getOrThrow()

                        // Send invoice email
                        sendInvoiceEmailUseCase.execute(invoice.id)

                        logger.info("Successfully billed tenant ${subscription.tenantId}")
                    } else {
                        // Handle payment failure
                        val error = invoiceResult.exceptionOrNull()
                        logger.error("Failed to bill tenant ${subscription.tenantId}", error)

                        handlePaymentFailureUseCase.execute(
                            subscription.tenantId,
                            reason = error?.message ?: "Unknown error"
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Failed to bill tenant ${subscription.tenantId}", e)

                    // Handle payment failure
                    handlePaymentFailureUseCase.execute(
                        subscription.tenantId,
                        reason = e.message ?: "Unknown error"
                    )
                }
            }

            logger.info("Daily billing process completed")
        } catch (e: Exception) {
            logger.error("Daily billing process failed", e)
        }
    }

    /**
     * Check trial expirations and attempt conversion to paid.
     * Runs every day at 1 AM.
     */
    @Scheduled(cron = "0 0 1 * * *")
    fun checkTrialExpirations() = runBlocking {
        logger.info("Checking trial expirations")

        try {
            val expiringTrials = subscriptionRepository
                .findTrialsExpiringToday()
                .getOrElse {
                    logger.error("Failed to fetch expiring trials", it)
                    emptyList()
                }

            logger.info("Found ${expiringTrials.size} trials expiring today")

            expiringTrials.forEach { subscription ->
                try {
                    logger.info("Handling trial expiration for tenant ${subscription.tenantId}")

                    // Convert trial to paid subscription or suspend
                    handleTrialExpirationUseCase.execute(subscription.tenantId)

                    logger.info("Trial expiration handled for tenant ${subscription.tenantId}")
                } catch (e: Exception) {
                    logger.error("Failed to handle trial expiration for ${subscription.tenantId}", e)
                }
            }

            logger.info("Trial expiration check completed")
        } catch (e: Exception) {
            logger.error("Trial expiration check failed", e)
        }
    }

    /**
     * Send payment reminders 3 days before due date.
     * Runs every day at 10 AM.
     */
    @Scheduled(cron = "0 0 10 * * *")
    fun sendPaymentReminders() = runBlocking {
        logger.info("Sending payment reminders")

        try {
            val reminderDate = LocalDate.now().plusDays(3)

            val upcomingBilling = subscriptionRepository
                .findByNextBillingDateBetween(reminderDate, reminderDate)
                .getOrElse {
                    logger.error("Failed to fetch subscriptions for payment reminders", it)
                    emptyList()
                }

            logger.info("Found ${upcomingBilling.size} subscriptions due in 3 days")

            upcomingBilling.forEach { subscription ->
                try {
                    logger.info("Sending payment reminder to tenant ${subscription.tenantId}")

                    sendPaymentReminderEmailUseCase.execute(subscription.tenantId)

                    logger.info("Payment reminder sent to tenant ${subscription.tenantId}")
                } catch (e: Exception) {
                    logger.error("Failed to send payment reminder to ${subscription.tenantId}", e)
                }
            }

            logger.info("Payment reminder process completed")
        } catch (e: Exception) {
            logger.error("Payment reminder process failed", e)
        }
    }
}
