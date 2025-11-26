package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.gym.domain.valueobjects.VAT
import java.time.Instant
import java.util.UUID

/**
 * Payment entity representing a financial transaction.
 * Can be linked to various payment contexts (subscription, PT session, etc.)
 * Belongs to a Tenant.
 */
data class Payment(
    val id: UUID,
    val tenantId: UUID, // For direct tenant filtering
    val memberId: UUID,
    val organizationId: UUID,
    val branchId: UUID,
    val amount: Money,
    val vat: VAT,
    val totalAmount: Money,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val invoiceNumber: String,
    val referenceNumber: String?,
    val subscriptionId: UUID?,
    val ptSessionId: UUID?,
    val description: String?,
    val paymentGatewayId: String?,
    val paymentGatewayResponse: String?,
    val paidAt: Instant?,
    val refundedAt: Instant?,
    val refundAmount: Money?,
    val refundReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(invoiceNumber.isNotBlank()) { "Invoice number cannot be blank" }
        require(amount.isPositive()) { "Payment amount must be positive" }
        require(!totalAmount.isNegative()) { "Total amount cannot be negative" }

        // Verify total amount calculation
        val calculatedTotal = amount + vat.amount
        require(totalAmount.amount == calculatedTotal.amount) {
            "Total amount must equal amount + VAT"
        }

        refundAmount?.let {
            require(it.isPositive()) { "Refund amount must be positive" }
            require(it.amount <= totalAmount.amount) {
                "Refund amount cannot exceed total amount"
            }
        }
    }

    fun isPending(): Boolean = status == PaymentStatus.PENDING

    fun isCompleted(): Boolean = status == PaymentStatus.COMPLETED

    fun isFailed(): Boolean = status == PaymentStatus.FAILED

    fun isRefunded(): Boolean = status == PaymentStatus.REFUNDED

    fun markAsPaid(paymentGatewayResponse: String? = null): Payment {
        require(status == PaymentStatus.PENDING) {
            "Only pending payments can be marked as paid"
        }
        val now = Instant.now()
        return copy(
            status = PaymentStatus.COMPLETED,
            paidAt = now,
            paymentGatewayResponse = paymentGatewayResponse,
            updatedAt = now
        )
    }

    fun markAsFailed(gatewayResponse: String? = null): Payment {
        require(status == PaymentStatus.PENDING) {
            "Only pending payments can be marked as failed"
        }
        return copy(
            status = PaymentStatus.FAILED,
            paymentGatewayResponse = gatewayResponse,
            updatedAt = Instant.now()
        )
    }

    fun refund(refundAmount: Money, reason: String): Payment {
        require(isCompleted()) { "Only completed payments can be refunded" }
        require(refundAmount.isPositive()) { "Refund amount must be positive" }
        require(refundAmount.amount <= totalAmount.amount) {
            "Refund amount cannot exceed total payment amount"
        }

        val now = Instant.now()
        return copy(
            status = PaymentStatus.REFUNDED,
            refundedAt = now,
            refundAmount = refundAmount,
            refundReason = reason,
            updatedAt = now
        )
    }

    fun retry(): Payment {
        require(isFailed()) { "Only failed payments can be retried" }
        return copy(
            status = PaymentStatus.PENDING,
            paymentGatewayResponse = null,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun create(
            tenantId: UUID,
            memberId: UUID,
            organizationId: UUID,
            branchId: UUID,
            amount: Money,
            vat: VAT,
            method: PaymentMethod,
            invoiceNumber: String,
            subscriptionId: UUID? = null,
            ptSessionId: UUID? = null,
            description: String? = null
        ): Payment {
            val totalAmount = amount + vat.amount
            val now = Instant.now()

            return Payment(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                memberId = memberId,
                organizationId = organizationId,
                branchId = branchId,
                amount = amount,
                vat = vat,
                totalAmount = totalAmount,
                method = method,
                status = PaymentStatus.PENDING,
                invoiceNumber = invoiceNumber,
                referenceNumber = null,
                subscriptionId = subscriptionId,
                ptSessionId = ptSessionId,
                description = description,
                paymentGatewayId = null,
                paymentGatewayResponse = null,
                paidAt = null,
                refundedAt = null,
                refundAmount = null,
                refundReason = null,
                createdAt = now,
                updatedAt = now
            )
        }

        fun generateInvoiceNumber(prefix: String = "INV"): String {
            val timestamp = System.currentTimeMillis()
            val random = (1000..9999).random()
            return "$prefix-$timestamp-$random"
        }
    }
}

/**
 * Payment method enumeration
 */
enum class PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_TRANSFER,
    APPLE_PAY,
    STCPAY,
    MADA,
    ONLINE
}

/**
 * Payment status enumeration
 */
enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
