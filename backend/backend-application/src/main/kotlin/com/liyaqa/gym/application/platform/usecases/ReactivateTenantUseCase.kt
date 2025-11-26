package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.application.platform.commands.ReactivateTenantCommand
import com.liyaqa.gym.domain.entities.tenant.TenantStatus
import com.liyaqa.gym.domain.exceptions.CannotReactivateTenantException
import com.liyaqa.gym.domain.exceptions.NoActiveSubscriptionException
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.domain.services.EmailService
import com.liyaqa.gym.domain.services.EventPublisher
import com.liyaqa.gym.domain.services.TenantAccessRevoker
import com.liyaqa.gym.domain.services.TenantReactivatedEvent
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Use case for reactivating a suspended or cancelled tenant.
 * Restores access and renews subscription if needed.
 */
@Service
class ReactivateTenantUseCase(
    private val tenantRepository: TenantRepository,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val accessRevoker: TenantAccessRevoker,
    private val emailService: EmailService,
    private val eventPublisher: EventPublisher
) {

    suspend fun execute(command: ReactivateTenantCommand): Result<Unit> {
        return try {
            // Find tenant
            val tenantOpt = tenantRepository.findById(command.tenantId).getOrThrow()
            if (!tenantOpt.isPresent) {
                return Result.failure(TenantNotFoundException(command.tenantId))
            }

            val tenant = tenantOpt.get()

            // Validate tenant can be reactivated
            if (tenant.status != TenantStatus.SUSPENDED && tenant.status != TenantStatus.CANCELLED) {
                return Result.failure(
                    CannotReactivateTenantException(
                        tenant.id,
                        "Tenant status is ${tenant.status}. Only SUSPENDED or CANCELLED tenants can be reactivated."
                    )
                )
            }

            // Find subscription (may be cancelled)
            val subscriptionOpt = subscriptionRepository.findActiveByTenant(command.tenantId).getOrThrow()
            if (!subscriptionOpt.isPresent) {
                // Try to find any subscription for this tenant
                val allSubscriptions = subscriptionRepository.findByTenant(
                    command.tenantId,
                    org.springframework.data.domain.PageRequest.of(0, 1)
                ).getOrThrow()

                if (allSubscriptions.content.isEmpty()) {
                    return Result.failure(NoActiveSubscriptionException(command.tenantId))
                }

                val subscription = allSubscriptions.content[0]

                // If subscription is cancelled, we need to renew it
                if (subscription.isCancelled()) {
                    // Calculate new end date
                    val today = LocalDate.now()
                    val newEndDate = when (subscription.billingCycle) {
                        com.liyaqa.gym.domain.entities.tenant.BillingCycle.MONTHLY -> today.plusMonths(1)
                        com.liyaqa.gym.domain.entities.tenant.BillingCycle.QUARTERLY -> today.plusMonths(3)
                        com.liyaqa.gym.domain.entities.tenant.BillingCycle.ANNUAL -> today.plusYears(1)
                    }

                    // Renew subscription
                    val renewedSubscription = subscription.renew(newEndDate, subscription.amount)
                    subscriptionRepository.save(renewedSubscription).getOrThrow()
                }
            } else {
                val subscription = subscriptionOpt.get()

                // Check if subscription is expired
                if (subscription.isExpired() || subscription.isSubscriptionExpired()) {
                    // Renew subscription
                    val newEndDate = when (subscription.billingCycle) {
                        com.liyaqa.gym.domain.entities.tenant.BillingCycle.MONTHLY ->
                            LocalDate.now().plusMonths(1)
                        com.liyaqa.gym.domain.entities.tenant.BillingCycle.QUARTERLY ->
                            LocalDate.now().plusMonths(3)
                        com.liyaqa.gym.domain.entities.tenant.BillingCycle.ANNUAL ->
                            LocalDate.now().plusYears(1)
                    }

                    val renewedSubscription = subscription.renew(newEndDate, subscription.amount)
                    subscriptionRepository.save(renewedSubscription).getOrThrow()
                } else if (subscription.isPastDue()) {
                    // Activate past due subscription
                    val activatedSubscription = subscription.activate()
                    subscriptionRepository.save(activatedSubscription).getOrThrow()
                }
            }

            // Activate tenant
            val activatedTenant = tenant.activate()
            tenantRepository.save(activatedTenant).getOrThrow()

            // Restore access
            accessRevoker.restoreTenantAccess(tenant.id).getOrThrow()

            // Publish event
            eventPublisher.publish(
                TenantReactivatedEvent(
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    occurredAt = Instant.now()
                )
            )

            // Send reactivation confirmation
            emailService.sendTenantReactivationConfirmation(activatedTenant)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
