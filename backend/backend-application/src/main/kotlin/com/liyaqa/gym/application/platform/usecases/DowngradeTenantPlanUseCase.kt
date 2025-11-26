package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.application.platform.commands.DowngradeTenantPlanCommand
import com.liyaqa.gym.domain.entities.tenant.BillingCycle
import com.liyaqa.gym.domain.entities.tenant.TenantSubscriptionPlan
import com.liyaqa.gym.domain.exceptions.InvalidPlanChangeException
import com.liyaqa.gym.domain.exceptions.NoActiveSubscriptionException
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.domain.services.EmailService
import com.liyaqa.gym.domain.services.EventPublisher
import com.liyaqa.gym.domain.services.TenantPlanDowngradedEvent
import com.liyaqa.gym.domain.valueobjects.Money
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Use case for downgrading a tenant's subscription plan.
 * Updates plan, features, and limits. Typically applies at end of billing period.
 */
@Service
class DowngradeTenantPlanUseCase(
    private val tenantRepository: TenantRepository,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val emailService: EmailService,
    private val eventPublisher: EventPublisher
) {

    suspend fun execute(command: DowngradeTenantPlanCommand): Result<Unit> {
        return try {
            // Find tenant
            val tenantOpt = tenantRepository.findById(command.tenantId).getOrThrow()
            if (!tenantOpt.isPresent) {
                return Result.failure(TenantNotFoundException(command.tenantId))
            }

            val tenant = tenantOpt.get()

            // Validate downgrade
            if (command.newPlan.ordinal >= tenant.subscriptionPlan.ordinal) {
                return Result.failure(
                    InvalidPlanChangeException(
                        "New plan must be a lower tier than current plan ${tenant.subscriptionPlan}"
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

            // Validate downgrade is possible (check current usage against new limits)
            validateDowngradeIsPossible(tenant, command.newPlan)

            // Determine effective date
            val effectiveDate = if (command.applyImmediately) {
                // Update tenant immediately
                val updatedTenant = tenant.downgradePlan(command.newPlan)
                tenantRepository.save(updatedTenant).getOrThrow()

                // Calculate new pricing
                val newAmount = calculatePlanPrice(command.newPlan, subscription.billingCycle)

                // Update subscription
                val updatedSubscription = subscription.changePlan(command.newPlan, newAmount)
                subscriptionRepository.save(updatedSubscription).getOrThrow()

                LocalDate.now()
            } else {
                // Schedule downgrade for end of billing period
                // This would typically involve creating a scheduled job
                // For now, we just set the effective date
                subscription.endDate
            }

            // Publish event
            eventPublisher.publish(
                TenantPlanDowngradedEvent(
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    oldPlan = oldPlan.displayName,
                    newPlan = command.newPlan.displayName,
                    occurredAt = Instant.now()
                )
            )

            // Send notice email
            emailService.sendPlanDowngradeNotice(
                tenant = tenant,
                newPlan = command.newPlan.displayName,
                effectiveDate = effectiveDate
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateDowngradeIsPossible(
        tenant: com.liyaqa.gym.domain.entities.tenant.Tenant,
        newPlan: TenantSubscriptionPlan
    ) {
        // Check if current usage fits within new plan limits
        // This would need to query actual usage data
        // For now, we'll do basic validation

        val newMaxBranches = newPlan.defaultMaxBranches
        val newMaxMembers = newPlan.defaultMaxMembers
        val newMaxStaff = newPlan.defaultMaxStaff

        // TODO: Query actual current usage and compare
        // For now, we'll just check against configured limits
        // In production, this should check actual usage:
        // - Count current branches
        // - Count current active members
        // - Count current staff

        // Placeholder validation
        if (newMaxBranches < tenant.maxBranches) {
            // Would need to check if current branches <= newMaxBranches
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
