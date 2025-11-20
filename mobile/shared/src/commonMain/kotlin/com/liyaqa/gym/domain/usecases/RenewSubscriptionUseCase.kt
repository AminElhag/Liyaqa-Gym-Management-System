package com.liyaqa.gym.domain.usecases

import com.liyaqa.gym.data.repositories.MemberRepository
import com.liyaqa.gym.data.repositories.SubscriptionRepository
import com.liyaqa.gym.domain.Subscription
import com.liyaqa.gym.domain.rules.CanRenewSubscriptionRule
import com.liyaqa.gym.domain.rules.RenewSubscriptionRuleInput
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use case to renew a subscription
 * Applies business rules before renewal
 */
class RenewSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val memberRepository: MemberRepository,
    private val renewSubscriptionRule: CanRenewSubscriptionRule = CanRenewSubscriptionRule()
) {
    /**
     * Execute the use case
     * @param subscriptionId The subscription ID to renew
     * @param planId The new plan ID
     * @param startDate The start date for the renewal (ISO format)
     * @param autoRenew Whether to enable auto-renewal
     * @return Result containing renewed Subscription or error
     */
    suspend operator fun invoke(
        subscriptionId: String,
        planId: String,
        startDate: String,
        autoRenew: Boolean = false
    ): Result<Subscription> {
        return try {
            // 1. Fetch subscription
            val subscriptionResult = subscriptionRepository.getSubscriptionById(subscriptionId)
            if (subscriptionResult.isFailure) {
                return Result.failure(
                    subscriptionResult.exceptionOrNull() ?: Exception("Failed to fetch subscription")
                )
            }
            val subscription = subscriptionResult.getOrThrow()

            // 2. Fetch member
            val memberResult = memberRepository.getMemberProfile(subscription.memberId)
            if (memberResult.isFailure) {
                return Result.failure(
                    memberResult.exceptionOrNull() ?: Exception("Failed to fetch member profile")
                )
            }
            val member = memberResult.getOrThrow()

            // 3. Apply business rule
            val ruleInput = RenewSubscriptionRuleInput(
                member = member,
                subscription = subscription,
                currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            )

            val ruleResult = renewSubscriptionRule.evaluate(ruleInput)
            if (ruleResult.isNotSatisfied()) {
                return Result.failure(
                    Exception(ruleResult.getReasonOrNull() ?: "Cannot renew subscription")
                )
            }

            // 4. Proceed with renewal
            subscriptionRepository.renewSubscription(subscriptionId, planId, startDate, autoRenew)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if a subscription can be renewed without performing the renewal
     * Useful for UI validation
     */
    suspend fun canRenew(subscriptionId: String): Result<Boolean> {
        return try {
            val subscriptionResult = subscriptionRepository.getSubscriptionById(subscriptionId)
            if (subscriptionResult.isFailure) {
                return Result.success(false)
            }
            val subscription = subscriptionResult.getOrThrow()

            val memberResult = memberRepository.getMemberProfile(subscription.memberId)
            if (memberResult.isFailure) {
                return Result.success(false)
            }
            val member = memberResult.getOrThrow()

            val ruleInput = RenewSubscriptionRuleInput(
                member = member,
                subscription = subscription,
                currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            )

            val ruleResult = renewSubscriptionRule.evaluate(ruleInput)
            Result.success(ruleResult.isSatisfied())

        } catch (e: Exception) {
            Result.success(false)
        }
    }
}
