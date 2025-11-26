package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.TenantPaymentReminderSentEvent
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
 * Use case for sending payment reminder emails to tenants.
 */
@Service
class SendPaymentReminderEmailUseCase(
    private val tenantRepository: TenantRepository,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val emailService: EmailService,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(SendPaymentReminderEmailUseCase::class.java)

    suspend fun execute(tenantId: UUID): Result<Unit> {
        return try {
            logger.info("Sending payment reminder for tenant: $tenantId")

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

            // Send payment reminder email
            emailService.sendPaymentReminderEmail(
                tenant = tenant,
                upcomingBillingDate = subscription.nextBillingDate,
                amount = subscription.amount.amount.toDouble()
            ).getOrThrow()

            // Publish event
            eventPublisher.publish(
                TenantPaymentReminderSentEvent(
                    tenantId = tenantId,
                    upcomingBillingDate = subscription.nextBillingDate.toString(),
                    amount = subscription.amount.amount.toDouble(),
                    occurredAt = Instant.now()
                )
            )

            logger.info("Payment reminder sent successfully for tenant: $tenantId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to send payment reminder for tenant: $tenantId", e)
            Result.failure(e)
        }
    }
}
