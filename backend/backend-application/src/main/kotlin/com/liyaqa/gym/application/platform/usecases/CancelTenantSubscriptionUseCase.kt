package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.application.platform.commands.CancelTenantSubscriptionCommand
import com.liyaqa.gym.domain.entities.tenant.TenantStatus
import com.liyaqa.gym.domain.exceptions.InvalidTenantStatusException
import com.liyaqa.gym.domain.exceptions.NoActiveSubscriptionException
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.domain.services.EmailService
import com.liyaqa.gym.domain.services.EventPublisher
import com.liyaqa.gym.domain.services.TenantAccessRevoker
import com.liyaqa.gym.domain.services.TenantSubscriptionCancelledEvent
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Use case for cancelling a tenant's subscription.
 * Can cancel immediately or at end of billing period.
 * Sends cancellation confirmation and revokes access if immediate.
 */
@Service
class CancelTenantSubscriptionUseCase(
    private val tenantRepository: TenantRepository,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val accessRevoker: TenantAccessRevoker,
    private val emailService: EmailService,
    private val eventPublisher: EventPublisher
) {

    suspend fun execute(command: CancelTenantSubscriptionCommand): Result<Unit> {
        return try {
            // Find tenant
            val tenantOpt = tenantRepository.findById(command.tenantId).getOrThrow()
            if (!tenantOpt.isPresent) {
                return Result.failure(TenantNotFoundException(command.tenantId))
            }

            val tenant = tenantOpt.get()

            // Validate tenant can be cancelled
            if (tenant.status == TenantStatus.CANCELLED) {
                return Result.failure(
                    InvalidTenantStatusException(
                        tenant.id,
                        tenant.status.name,
                        "Tenant is already cancelled"
                    )
                )
            }

            // Find active subscription
            val subscriptionOpt = subscriptionRepository.findActiveByTenant(command.tenantId).getOrThrow()
            if (!subscriptionOpt.isPresent) {
                return Result.failure(NoActiveSubscriptionException(command.tenantId))
            }

            val subscription = subscriptionOpt.get()

            // Determine cancellation date
            val cancellationDate = if (command.cancelImmediately) {
                // Cancel immediately
                val updatedTenant = tenant.cancel()
                tenantRepository.save(updatedTenant).getOrThrow()

                // Revoke access
                accessRevoker.revokeTenantAccess(tenant.id).getOrThrow()

                LocalDate.now()
            } else {
                // Cancel at end of billing period
                // Tenant retains access until subscription end date
                subscription.endDate
            }

            // Cancel subscription
            val cancelledSubscription = subscription.cancel(command.reason)
            subscriptionRepository.save(cancelledSubscription).getOrThrow()

            // Publish event
            eventPublisher.publish(
                TenantSubscriptionCancelledEvent(
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    reason = command.reason,
                    occurredAt = Instant.now()
                )
            )

            // Send cancellation confirmation
            emailService.sendSubscriptionCancellationConfirmation(
                tenant = tenant,
                endDate = cancellationDate
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
