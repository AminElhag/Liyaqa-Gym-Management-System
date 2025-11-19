package com.liyaqa.gym.application.financial.commands

import java.math.BigDecimal
import java.util.UUID

/**
 * Command for processing a payment refund.
 *
 * @property paymentId The payment to refund
 * @property amount The amount to refund (can be partial or full)
 * @property reason The reason for the refund
 * @property validatePolicy Whether to validate refund policy (e.g., time limits)
 */
data class ProcessRefundCommand(
    val paymentId: UUID,
    val amount: BigDecimal,
    val reason: String,
    val validatePolicy: Boolean = true
)
