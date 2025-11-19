package com.liyaqa.gym.application.subscription.commands

import com.liyaqa.gym.domain.entities.PaymentMethod
import java.util.UUID

/**
 * Command for renewing an existing subscription.
 *
 * @property subscriptionId The unique identifier of the subscription to renew
 * @property paymentMethod The payment method to use for renewal payment
 * @property paymentMetadata Additional metadata for payment processing
 */
data class RenewSubscriptionCommand(
    val subscriptionId: UUID,
    val paymentMethod: PaymentMethod,
    val paymentMetadata: Map<String, Any> = emptyMap()
)
