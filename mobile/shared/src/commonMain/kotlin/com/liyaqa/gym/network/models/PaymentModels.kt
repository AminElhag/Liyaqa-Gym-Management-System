package com.liyaqa.gym.network.models

import kotlinx.serialization.Serializable

/**
 * Payment-related request/response models
 */

@Serializable
data class PaymentRequest(
    val subscriptionId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val description: String? = null
)

@Serializable
data class PaymentResponse(
    val id: String,
    val memberId: String,
    val subscriptionId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val transactionId: String? = null,
    val description: String? = null,
    val paidAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class InvoiceResponse(
    val id: String,
    val memberId: String,
    val subscriptionId: String? = null,
    val amount: Double,
    val paidAmount: Double,
    val status: InvoiceStatus,
    val dueDate: String,
    val paidAt: String? = null,
    val items: List<InvoiceItem>,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class InvoiceItem(
    val description: String,
    val quantity: Int,
    val unitPrice: Double,
    val amount: Double
)

@Serializable
data class InvoiceListResponse(
    val content: List<InvoiceResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)

@Serializable
enum class PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_TRANSFER,
    MOBILE_PAYMENT,
    OTHER
}

@Serializable
enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}

@Serializable
enum class InvoiceStatus {
    PENDING,
    PAID,
    PARTIALLY_PAID,
    OVERDUE,
    CANCELLED
}
