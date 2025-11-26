package com.liyaqa.gym.domain.entities.tenant

import com.liyaqa.gym.domain.valueobjects.InvoiceLineItem
import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * PlatformInvoice entity representing B2B invoices for tenant subscriptions.
 * Different from regular Invoice which is for gym member payments.
 * This invoice is for billing the gym business (tenant) for platform usage.
 */
data class PlatformInvoice(
    val id: UUID,
    val tenantId: UUID,
    val invoiceNumber: String,
    val amount: Money,
    val vatAmount: Money,
    val totalAmount: Money,
    val status: InvoiceStatus,
    val dueDate: LocalDate,
    val issueDate: LocalDate,
    val paidAt: Instant?,
    val items: List<InvoiceLineItem>,
    val zatcaClearanceId: String?, // ZATCA e-invoice clearance ID
    val qrCodeData: String?, // QR code for ZATCA compliance
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(invoiceNumber.isNotBlank()) { "Invoice number cannot be blank" }
        require(items.isNotEmpty()) { "Invoice must have at least one line item" }
        require(amount.isPositive() || amount.isZero()) { "Amount cannot be negative" }
        require(!vatAmount.isNegative()) { "VAT amount cannot be negative" }
        require(!totalAmount.isNegative()) { "Total amount cannot be negative" }
        require(!dueDate.isBefore(issueDate)) { "Due date cannot be before issue date" }

        // Verify total amount calculation
        val calculatedTotal = amount + vatAmount
        require(totalAmount.amount == calculatedTotal.amount) {
            "Total amount must equal amount + VAT"
        }

        // Verify amount matches line items
        val lineItemsTotal = items.fold(Money.zero(amount.currency.currencyCode)) { acc, item ->
            acc + item.totalAmount
        }
        require(amount.amount == lineItemsTotal.amount) {
            "Amount must equal sum of line items"
        }
    }

    fun isPending(): Boolean = status == InvoiceStatus.PENDING

    fun isPaid(): Boolean = status == InvoiceStatus.PAID

    fun isOverdue(): Boolean {
        if (status == InvoiceStatus.OVERDUE) return true
        return dueDate.isBefore(LocalDate.now()) && status == InvoiceStatus.PENDING
    }

    fun isCancelled(): Boolean = status == InvoiceStatus.CANCELLED

    fun isVoid(): Boolean = status == InvoiceStatus.VOID

    fun daysUntilDue(): Long {
        val today = LocalDate.now()
        return if (dueDate.isAfter(today)) {
            java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)
        } else {
            0
        }
    }

    fun daysOverdue(): Long {
        val today = LocalDate.now()
        return if (today.isAfter(dueDate)) {
            java.time.temporal.ChronoUnit.DAYS.between(dueDate, today)
        } else {
            0
        }
    }

    fun markAsPaid(): PlatformInvoice {
        require(isPending() || isOverdue()) {
            "Only pending or overdue invoices can be marked as paid"
        }
        return copy(
            status = InvoiceStatus.PAID,
            paidAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    fun markAsOverdue(): PlatformInvoice {
        require(isPending()) { "Only pending invoices can be marked as overdue" }
        return copy(
            status = InvoiceStatus.OVERDUE,
            updatedAt = Instant.now()
        )
    }

    fun cancel(reason: String? = null): PlatformInvoice {
        require(!isPaid() && !isCancelled()) {
            "Only unpaid invoices can be cancelled"
        }
        val updatedNotes = if (reason != null) {
            "${notes ?: ""}\nCancellation reason: $reason"
        } else {
            notes
        }
        return copy(
            status = InvoiceStatus.CANCELLED,
            notes = updatedNotes,
            updatedAt = Instant.now()
        )
    }

    fun void(): PlatformInvoice {
        require(isPending()) { "Only pending invoices can be voided" }
        return copy(
            status = InvoiceStatus.VOID,
            updatedAt = Instant.now()
        )
    }

    fun addZatcaClearance(clearanceId: String, qrCode: String): PlatformInvoice {
        return copy(
            zatcaClearanceId = clearanceId,
            qrCodeData = qrCode,
            updatedAt = Instant.now()
        )
    }

    fun updateNotes(newNotes: String): PlatformInvoice {
        return copy(
            notes = newNotes,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun create(
            tenantId: UUID,
            amount: Money,
            vatAmount: Money,
            dueDate: LocalDate,
            items: List<InvoiceLineItem>,
            notes: String? = null
        ): PlatformInvoice {
            val now = Instant.now()
            val today = LocalDate.now()
            val totalAmount = amount + vatAmount
            val invoiceNumber = generateInvoiceNumber()

            return PlatformInvoice(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                invoiceNumber = invoiceNumber,
                amount = amount,
                vatAmount = vatAmount,
                totalAmount = totalAmount,
                status = InvoiceStatus.PENDING,
                dueDate = dueDate,
                issueDate = today,
                paidAt = null,
                items = items,
                zatcaClearanceId = null,
                qrCodeData = null,
                notes = notes,
                createdAt = now,
                updatedAt = now
            )
        }

        fun createSubscriptionInvoice(
            tenantId: UUID,
            subscription: TenantSubscription,
            description: String
        ): PlatformInvoice {
            val amount = subscription.amount
            val vatRate = 0.15 // 15% VAT for Saudi Arabia
            val vatAmount = amount * vatRate.toBigDecimal()

            val lineItems = listOf(
                InvoiceLineItem.createSingle(
                    description = description,
                    descriptionArabic = null,
                    price = amount
                )
            )

            val dueDate = LocalDate.now().plusDays(30) // 30 days payment term

            return create(
                tenantId = tenantId,
                amount = amount,
                vatAmount = vatAmount,
                dueDate = dueDate,
                items = lineItems,
                notes = "Subscription: ${subscription.plan.displayName} - ${subscription.billingCycle}"
            )
        }

        /**
         * Generates a unique invoice number for platform invoices.
         * Format: PI-YYYYMMDD-NNNNNN (PI = Platform Invoice)
         */
        fun generateInvoiceNumber(): String {
            val date = LocalDate.now().toString().replace("-", "")
            val random = (100000..999999).random()
            return "PI-$date-$random"
        }
    }
}

/**
 * Platform invoice status enumeration
 */
enum class InvoiceStatus {
    PENDING,      // Invoice issued, awaiting payment
    PAID,         // Payment received
    OVERDUE,      // Past due date, not paid
    CANCELLED,    // Cancelled before payment
    VOID,         // Voided/invalidated
    REFUNDED      // Payment refunded
}
