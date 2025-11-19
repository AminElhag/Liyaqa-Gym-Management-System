package com.liyaqa.gym.application.subscription.commands

import com.liyaqa.gym.domain.entities.PaymentMethod
import java.util.UUID

/**
 * Command for upgrading a subscription to a different plan.
 *
 * @property subscriptionId The unique identifier of the subscription to upgrade
 * @property newPlanId The unique identifier of the new membership plan
 * @property paymentMethod The payment method to use for the prorated payment
 * @property paymentMetadata Additional metadata for payment processing
 */
data class UpgradeSubscriptionCommand(
    val subscriptionId: UUID,
    val newPlanId: UUID,
    val paymentMethod: PaymentMethod,
    val paymentMetadata: Map<String, Any> = emptyMap()
)
