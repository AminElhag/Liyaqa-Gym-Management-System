package com.liyaqa.gym.application.financial.commands

import com.liyaqa.gym.domain.entities.PaymentMethod
import java.math.BigDecimal
import java.util.UUID

/**
 * Command for processing a payment with VAT calculation.
 *
 * @property memberId The member making the payment
 * @property organizationId The organization identifier
 * @property branchId The branch identifier
 * @property amount The payment amount (excluding VAT)
 * @property currency The currency code (e.g., "SAR")
 * @property method The payment method to use
 * @property subscriptionId Optional subscription ID if payment is for a subscription
 * @property ptSessionId Optional PT session ID if payment is for a PT session
 * @property description Payment description
 * @property metadata Additional metadata for the payment gateway
 */
data class ProcessPaymentCommand(
    val memberId: UUID,
    val organizationId: UUID,
    val branchId: UUID,
    val amount: BigDecimal,
    val currency: String = "SAR",
    val method: PaymentMethod,
    val subscriptionId: UUID? = null,
    val ptSessionId: UUID? = null,
    val description: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)
