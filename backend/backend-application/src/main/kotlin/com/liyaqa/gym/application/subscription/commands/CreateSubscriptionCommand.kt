package com.liyaqa.gym.application.subscription.commands

import com.liyaqa.gym.domain.entities.PaymentMethod
import java.time.LocalDate
import java.util.UUID

/**
 * Command for creating a new subscription.
 *
 * @property memberId The unique identifier of the member
 * @property planId The unique identifier of the membership plan
 * @property startDate The start date of the subscription (defaults to today)
 * @property autoRenew Whether the subscription should auto-renew
 * @property paymentMethod The payment method to use for the initial payment
 * @property paymentMetadata Additional metadata for payment processing
 */
data class CreateSubscriptionCommand(
    val memberId: UUID,
    val planId: UUID,
    val startDate: LocalDate = LocalDate.now(),
    val autoRenew: Boolean = false,
    val paymentMethod: PaymentMethod,
    val paymentMetadata: Map<String, Any> = emptyMap()
) {
    init {
        require(!startDate.isBefore(LocalDate.now().minusDays(1))) {
            "Start date cannot be in the past"
        }
    }
}
