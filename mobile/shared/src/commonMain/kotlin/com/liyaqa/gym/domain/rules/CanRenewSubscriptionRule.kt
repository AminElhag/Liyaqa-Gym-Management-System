package com.liyaqa.gym.domain.rules

import com.liyaqa.gym.domain.*
import kotlinx.datetime.*

/**
 * Input data for CanRenewSubscriptionRule
 */
data class RenewSubscriptionRuleInput(
    val member: Member,
    val subscription: Subscription,
    val renewalWindowDays: Int = 7, // Can renew within 7 days of expiry
    val currentDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
)

/**
 * Business rule to check if a subscription can be renewed
 * Checks:
 * 1. Member is active
 * 2. Subscription exists and is in renewable status
 * 3. Renewal is within allowed window (e.g., within 7 days of expiry or after)
 * 4. Subscription is not already active with future end date
 */
class CanRenewSubscriptionRule : BusinessRule<RenewSubscriptionRuleInput> {

    override fun evaluate(input: RenewSubscriptionRuleInput): RuleResult {
        // Check member is active
        if (!input.member.isActive()) {
            return RuleResult.NotSatisfied("Member account is not active")
        }

        // Check subscription status is renewable
        if (input.subscription.status == SubscriptionStatus.CANCELLED) {
            return RuleResult.NotSatisfied("Cancelled subscriptions cannot be renewed. Please create a new subscription.")
        }

        val endDate = input.subscription.endDate
            ?: return RuleResult.NotSatisfied("Unlimited subscriptions cannot be renewed")

        // Calculate days until expiry
        val daysUntilExpiry = endDate.toEpochDays() - input.currentDate.toEpochDays()

        // Check renewal window
        if (daysUntilExpiry > input.renewalWindowDays) {
            return RuleResult.NotSatisfied(
                "Subscription can only be renewed within ${input.renewalWindowDays} days of expiry. " +
                "Current subscription expires in $daysUntilExpiry days."
            )
        }

        // For expired subscriptions, check they haven't been expired too long (e.g., grace period)
        if (daysUntilExpiry < -30) { // 30 days grace period
            return RuleResult.NotSatisfied(
                "Subscription expired more than 30 days ago. Please create a new subscription."
            )
        }

        return RuleResult.Satisfied
    }
}
