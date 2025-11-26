package com.liyaqa.gym.domain.entities.tenant

import com.liyaqa.gym.domain.entities.PaymentMethod
import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * TenantSubscription entity representing B2B billing for platform usage.
 * Tracks subscription status and renewal for each tenant.
 */
data class TenantSubscription(
    val id: UUID,
    val tenantId: UUID,
    val plan: TenantSubscriptionPlan,
    val status: SubscriptionStatus,
    val amount: Money, // Monthly/Annual fee based on billing cycle
    val billingCycle: BillingCycle,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val autoRenew: Boolean,
    val paymentMethod: PaymentMethod?,
    val nextBillingDate: LocalDate,
    val trialEndsAt: LocalDate?,
    val cancelledAt: Instant?,
    val cancellationReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(!endDate.isBefore(startDate)) { "End date cannot be before start date" }
        require(!nextBillingDate.isBefore(startDate)) { "Next billing date cannot be before start date" }
        require(amount.isPositive() || amount.isZero()) { "Amount cannot be negative" }

        if (cancelledAt != null) {
            require(status == SubscriptionStatus.CANCELLED) {
                "Cancelled date should only be set when status is CANCELLED"
            }
        }

        if (trialEndsAt != null) {
            require(status == SubscriptionStatus.TRIAL || status == SubscriptionStatus.TRIALING) {
                "Trial end date should only be set during trial period"
            }
        }
    }

    fun isActive(): Boolean = status == SubscriptionStatus.ACTIVE

    fun isTrial(): Boolean = status == SubscriptionStatus.TRIAL || status == SubscriptionStatus.TRIALING

    fun isCancelled(): Boolean = status == SubscriptionStatus.CANCELLED

    fun isExpired(): Boolean = status == SubscriptionStatus.EXPIRED

    fun isPastDue(): Boolean = status == SubscriptionStatus.PAST_DUE

    fun isTrialExpired(): Boolean = trialEndsAt?.isBefore(LocalDate.now()) ?: false

    fun isSubscriptionExpired(): Boolean = LocalDate.now().isAfter(endDate)

    fun daysUntilExpiration(): Long {
        val today = LocalDate.now()
        return if (endDate.isAfter(today)) {
            java.time.temporal.ChronoUnit.DAYS.between(today, endDate)
        } else {
            0
        }
    }

    fun activate(): TenantSubscription {
        require(status == SubscriptionStatus.TRIAL || status == SubscriptionStatus.PAST_DUE) {
            "Only trial or past due subscriptions can be activated"
        }
        return copy(
            status = SubscriptionStatus.ACTIVE,
            updatedAt = Instant.now()
        )
    }

    fun cancel(reason: String): TenantSubscription {
        require(!isCancelled()) { "Subscription is already cancelled" }
        val now = Instant.now()
        return copy(
            status = SubscriptionStatus.CANCELLED,
            autoRenew = false,
            cancelledAt = now,
            cancellationReason = reason,
            updatedAt = now
        )
    }

    fun markAsPastDue(): TenantSubscription {
        require(isActive()) { "Only active subscriptions can be marked as past due" }
        return copy(
            status = SubscriptionStatus.PAST_DUE,
            updatedAt = Instant.now()
        )
    }

    fun markAsExpired(): TenantSubscription {
        require(isSubscriptionExpired()) { "Subscription has not expired yet" }
        return copy(
            status = SubscriptionStatus.EXPIRED,
            autoRenew = false,
            updatedAt = Instant.now()
        )
    }

    fun renew(endDate: LocalDate, amount: Money): TenantSubscription {
        require(autoRenew || !isExpired()) { "Cannot renew expired subscription without auto-renew" }

        val nextBillingDate = when (billingCycle) {
            BillingCycle.MONTHLY -> endDate.plusMonths(1)
            BillingCycle.QUARTERLY -> endDate.plusMonths(3)
            BillingCycle.ANNUAL -> endDate.plusYears(1)
        }

        return copy(
            status = SubscriptionStatus.ACTIVE,
            endDate = endDate,
            amount = amount,
            nextBillingDate = nextBillingDate,
            updatedAt = Instant.now()
        )
    }

    fun changePlan(newPlan: TenantSubscriptionPlan, newAmount: Money): TenantSubscription {
        return copy(
            plan = newPlan,
            amount = newAmount,
            updatedAt = Instant.now()
        )
    }

    fun updatePaymentMethod(newPaymentMethod: PaymentMethod): TenantSubscription {
        return copy(
            paymentMethod = newPaymentMethod,
            updatedAt = Instant.now()
        )
    }

    fun enableAutoRenew(): TenantSubscription {
        require(!autoRenew) { "Auto-renew is already enabled" }
        return copy(
            autoRenew = true,
            updatedAt = Instant.now()
        )
    }

    fun disableAutoRenew(): TenantSubscription {
        require(autoRenew) { "Auto-renew is already disabled" }
        return copy(
            autoRenew = false,
            updatedAt = Instant.now()
        )
    }

    fun convertFromTrial(): TenantSubscription {
        require(isTrial()) { "Only trial subscriptions can be converted" }
        require(paymentMethod != null) { "Payment method must be set before converting from trial" }

        return copy(
            status = SubscriptionStatus.ACTIVE,
            trialEndsAt = null,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun createTrial(
            tenantId: UUID,
            plan: TenantSubscriptionPlan,
            billingCycle: BillingCycle
        ): TenantSubscription {
            val now = Instant.now()
            val today = LocalDate.now()
            val trialEndDate = today.plusDays(14) // 14-day trial

            val amount = calculateAmount(plan, billingCycle)
            val nextBillingDate = trialEndDate.plusDays(1)

            return TenantSubscription(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                plan = plan,
                status = SubscriptionStatus.TRIAL,
                amount = amount,
                billingCycle = billingCycle,
                startDate = today,
                endDate = trialEndDate,
                autoRenew = true,
                paymentMethod = null,
                nextBillingDate = nextBillingDate,
                trialEndsAt = trialEndDate,
                cancelledAt = null,
                cancellationReason = null,
                createdAt = now,
                updatedAt = now
            )
        }

        fun createActive(
            tenantId: UUID,
            plan: TenantSubscriptionPlan,
            billingCycle: BillingCycle,
            paymentMethod: PaymentMethod
        ): TenantSubscription {
            val now = Instant.now()
            val today = LocalDate.now()

            val endDate = when (billingCycle) {
                BillingCycle.MONTHLY -> today.plusMonths(1)
                BillingCycle.QUARTERLY -> today.plusMonths(3)
                BillingCycle.ANNUAL -> today.plusYears(1)
            }

            val amount = calculateAmount(plan, billingCycle)
            val nextBillingDate = endDate

            return TenantSubscription(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                plan = plan,
                status = SubscriptionStatus.ACTIVE,
                amount = amount,
                billingCycle = billingCycle,
                startDate = today,
                endDate = endDate,
                autoRenew = true,
                paymentMethod = paymentMethod,
                nextBillingDate = nextBillingDate,
                trialEndsAt = null,
                cancelledAt = null,
                cancellationReason = null,
                createdAt = now,
                updatedAt = now
            )
        }

        private fun calculateAmount(plan: TenantSubscriptionPlan, billingCycle: BillingCycle): Money {
            val monthlyPrice = plan.monthlyPriceSAR

            val totalAmount = when (billingCycle) {
                BillingCycle.MONTHLY -> monthlyPrice
                BillingCycle.QUARTERLY -> (monthlyPrice * 3 * 0.95).toInt() // 5% discount
                BillingCycle.ANNUAL -> (monthlyPrice * 12 * 0.85).toInt() // 15% discount
            }

            return Money.sar(totalAmount.toDouble())
        }
    }
}

/**
 * Subscription status enumeration for tenant subscriptions
 */
enum class SubscriptionStatus {
    TRIAL,          // In trial period
    TRIALING,       // Alternative form of trial
    ACTIVE,         // Active paid subscription
    PAST_DUE,       // Payment failed, grace period
    CANCELLED,      // Cancelled by tenant
    EXPIRED,        // Subscription ended naturally
    INCOMPLETE      // Payment setup incomplete
}
