package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.application.platform.commands.UpgradeTenantPlanCommand
import com.liyaqa.gym.domain.entities.tenant.BillingCycle
import com.liyaqa.gym.domain.entities.tenant.TenantSubscriptionPlan
import com.liyaqa.gym.domain.exceptions.InvalidPlanChangeException
import com.liyaqa.gym.domain.exceptions.NoActiveSubscriptionException
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.domain.services.EmailService
import com.liyaqa.gym.domain.services.EventPublisher
import com.liyaqa.gym.domain.services.TenantPlanUpgradedEvent
import com.liyaqa.gym.domain.valueobjects.Money
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Use case for upgrading a tenant's subscription plan.
 * Updates plan, features, limits, and processes prorated billing if applicable.
 */
@Service
class UpgradeTenantPlanUseCase(
    private val tenantRepository: TenantRepository,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val emailService: EmailService,
    private val eventPublisher: EventPublisher
) {

    suspend fun execute(command: UpgradeTenantPlanCommand): Result<Unit> {
        return try {
            // Find tenant
            val tenantOpt = tenantRepository.findById(command.tenantId).getOrThrow()
            if (!tenantOpt.isPresent) {
                return Result.failure(TenantNotFoundException(command.tenantId))
            }

            val tenant = tenantOpt.get()

            // Validate upgrade
            if (command.newPlan.ordinal <= tenant.subscriptionPlan.ordinal) {
                return Result.failure(
                    InvalidPlanChangeException(
                        "New plan must be a higher tier than current plan ${tenant.subscriptionPlan}"
                    )
                )
            }

            // Find active subscription
            val subscriptionOpt = subscriptionRepository.findActiveByTenant(command.tenantId).getOrThrow()
            if (!subscriptionOpt.isPresent) {
                return Result.failure(NoActiveSubscriptionException(command.tenantId))
            }

            val subscription = subscriptionOpt.get()
            val oldPlan = tenant.subscriptionPlan

            // Update tenant with new plan
            val updatedTenant = tenant.upgradePlan(command.newPlan)
            tenantRepository.save(updatedTenant).getOrThrow()

            // Calculate new pricing
            val newAmount = calculatePlanPrice(command.newPlan, subscription.billingCycle)

            // Update subscription
            val updatedSubscription = subscription.changePlan(command.newPlan, newAmount)
            subscriptionRepository.save(updatedSubscription).getOrThrow()

            // Determine effective date
            val effectiveDate = if (command.applyImmediately) {
                LocalDate.now()
            } else {
                subscription.endDate
            }

            // Publish event
            eventPublisher.publish(
                TenantPlanUpgradedEvent(
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    oldPlan = oldPlan.displayName,
                    newPlan = command.newPlan.displayName,
                    occurredAt = Instant.now()
                )
            )

            // Send confirmation email
            emailService.sendPlanUpgradeConfirmation(
                tenant = updatedTenant,
                newPlan = command.newPlan.displayName,
                effectiveDate = effectiveDate
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculatePlanPrice(
        plan: TenantSubscriptionPlan,
        billingCycle: BillingCycle
    ): Money {
        val monthlyPrice = plan.monthlyPriceSAR.toDouble()

        val amount = when (billingCycle) {
            BillingCycle.MONTHLY -> monthlyPrice
            BillingCycle.QUARTERLY -> monthlyPrice * 3 * 0.95 // 5% discount
            BillingCycle.ANNUAL -> monthlyPrice * 12 * 0.85 // 15% discount
        }

        return Money.sar(amount)
    }
}
