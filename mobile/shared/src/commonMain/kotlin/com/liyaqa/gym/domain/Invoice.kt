package com.liyaqa.gym.domain

import com.liyaqa.gym.network.models.InvoiceStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Invoice entity representing a billing invoice.
 */
@Serializable
data class Invoice(
    val id: String,
    val memberId: String,
    val subscriptionId: String? = null,
    val amount: Double,
    val paidAmount: Double,
    val status: InvoiceStatus,
    val dueDate: LocalDate,
    val paidAt: Instant? = null,
    val items: List<InvoiceItem>,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun isPending(): Boolean = status == InvoiceStatus.PENDING

    fun isPaid(): Boolean = status == InvoiceStatus.PAID

    fun isPartiallyPaid(): Boolean = status == InvoiceStatus.PARTIALLY_PAID

    fun isOverdue(): Boolean = status == InvoiceStatus.OVERDUE

    fun isCancelled(): Boolean = status == InvoiceStatus.CANCELLED

    fun remainingAmount(): Double = amount - paidAmount

    fun isOverdue(currentDate: LocalDate): Boolean {
        return dueDate < currentDate && !isPaid()
    }

    fun canPay(): Boolean {
        return status == InvoiceStatus.PENDING ||
               status == InvoiceStatus.PARTIALLY_PAID ||
               status == InvoiceStatus.OVERDUE
    }

    fun statusDisplayText(): String {
        return when (status) {
            InvoiceStatus.PENDING -> "Pending"
            InvoiceStatus.PAID -> "Paid"
            InvoiceStatus.PARTIALLY_PAID -> "Partially Paid"
            InvoiceStatus.OVERDUE -> "Overdue"
            InvoiceStatus.CANCELLED -> "Cancelled"
        }
    }
}

/**
 * Invoice item representing a line item in an invoice
 */
@Serializable
data class InvoiceItem(
    val description: String,
    val quantity: Int,
    val unitPrice: Double,
    val amount: Double
) {
    fun total(): Double = quantity * unitPrice
}
