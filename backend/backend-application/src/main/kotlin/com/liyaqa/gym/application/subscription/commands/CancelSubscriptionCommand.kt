package com.liyaqa.gym.application.subscription.commands

import java.util.UUID

/**
 * Command for cancelling a subscription.
 *
 * @property subscriptionId The unique identifier of the subscription to cancel
 * @property reason The reason for cancellation (for exit survey)
 * @property immediate Whether to cancel immediately or at the end of the current period
 */
data class CancelSubscriptionCommand(
    val subscriptionId: UUID,
    val reason: String? = null,
    val immediate: Boolean = false
) {
    init {
        if (reason != null) {
            require(reason.isNotBlank()) {
                "Cancellation reason cannot be blank if provided"
            }
        }
    }
}
