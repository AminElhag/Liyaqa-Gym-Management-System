package com.liyaqa.gym.domain

import com.liyaqa.gym.network.models.PaymentMethod
import com.liyaqa.gym.network.models.PaymentStatus
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Payment entity representing a payment transaction.
 */
@Serializable
data class Payment(
    val id: String,
    val memberId: String,
    val subscriptionId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val transactionId: String? = null,
    val description: String? = null,
    val paidAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun isPending(): Boolean = status == PaymentStatus.PENDING

    fun isCompleted(): Boolean = status == PaymentStatus.COMPLETED

    fun isFailed(): Boolean = status == PaymentStatus.FAILED

    fun isRefunded(): Boolean = status == PaymentStatus.REFUNDED

    fun isCancelled(): Boolean = status == PaymentStatus.CANCELLED

    fun canRefund(): Boolean = status == PaymentStatus.COMPLETED

    fun statusDisplayText(): String {
        return when (status) {
            PaymentStatus.PENDING -> "Pending"
            PaymentStatus.COMPLETED -> "Completed"
            PaymentStatus.FAILED -> "Failed"
            PaymentStatus.REFUNDED -> "Refunded"
            PaymentStatus.CANCELLED -> "Cancelled"
        }
    }

    fun paymentMethodDisplayText(): String {
        return when (paymentMethod) {
            PaymentMethod.CASH -> "Cash"
            PaymentMethod.CREDIT_CARD -> "Credit Card"
            PaymentMethod.DEBIT_CARD -> "Debit Card"
            PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
            PaymentMethod.MOBILE_PAYMENT -> "Mobile Payment"
            PaymentMethod.OTHER -> "Other"
        }
    }
}
