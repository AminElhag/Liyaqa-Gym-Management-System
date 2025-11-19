package com.liyaqa.gym.application.subscription.commands

import java.time.LocalDate
import java.util.UUID

/**
 * Command for freezing/pausing a subscription.
 *
 * @property subscriptionId The unique identifier of the subscription to freeze
 * @property freezeUntil The date until which the subscription should be frozen
 * @property reason Optional reason for freezing the subscription
 */
data class FreezeSubscriptionCommand(
    val subscriptionId: UUID,
    val freezeUntil: LocalDate,
    val reason: String? = null
) {
    init {
        require(freezeUntil.isAfter(LocalDate.now())) {
            "Freeze end date must be in the future"
        }
    }
}
