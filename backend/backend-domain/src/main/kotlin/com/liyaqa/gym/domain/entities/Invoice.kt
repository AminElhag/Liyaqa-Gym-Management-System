package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.InvoiceLineItem
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.gym.domain.valueobjects.VAT
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Invoice entity representing a ZATCA-compliant tax invoice.
 *
 * This entity follows Saudi Arabia's ZATCA (Zakat, Tax and Customs Authority) requirements
 * for e-invoicing including:
 * - Seller information (gym name, VAT registration number)
 * - Buyer information (member name, national ID or VAT number if B2B)
 * - Line items with descriptions
 * - Subtotal, VAT breakdown, total
 * - QR code for invoice verification
 * - ZATCA clearance UUID
 */
data class Invoice(
    val id: UUID,
    val invoiceNumber: String,
    val organizationId: UUID,
    val branchId: UUID,
    val memberId: UUID,

    // Seller information (gym/branch)
    val sellerName: String,
    val sellerNameArabic: String?,
    val sellerVatRegistrationNumber: String,
    val sellerAddress: String,
    val sellerAddressArabic: String?,

    // Buyer information (member)
    val buyerName: String,
    val buyerNameArabic: String?,
    val buyerNationalId: String?,
    val buyerVatNumber: String?, // For B2B transactions
    val buyerAddress: String?,

    // Invoice details
    val lineItems: List<InvoiceLineItem>,
    val subtotal: Money,
    val vat: VAT,
    val totalAmount: Money,
    val issueDate: LocalDate,
    val dueDate: LocalDate?,
    val notes: String?,

    // ZATCA compliance fields
    val qrCode: String?, // Base64 encoded QR code data
    val zatcaClearanceUUID: String?, // UUID from ZATCA after clearance
    val zatcaStatus: ZATCAStatus,
    val zatcaSubmittedAt: Instant?,
    val zatcaClearedAt: Instant?,
    val zatcaErrorMessage: String?,

    // Storage paths
    val xmlFilePath: String?,
    val pdfFilePath: String?,

    // Audit fields
    val status: InvoiceStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(invoiceNumber.isNotBlank()) { "Invoice number cannot be blank" }
        require(sellerName.isNotBlank()) { "Seller name cannot be blank" }
        require(sellerVatRegistrationNumber.isNotBlank()) { "Seller VAT registration number cannot be blank" }
        require(buyerName.isNotBlank()) { "Buyer name cannot be blank" }
        require(lineItems.isNotEmpty()) { "Invoice must have at least one line item" }
        require(subtotal.isPositive() || subtotal.isZero()) { "Subtotal cannot be negative" }
        require(!totalAmount.isNegative()) { "Total amount cannot be negative" }
        require(dueDate?.isBefore(issueDate) != true) { "Due date cannot be before issue date" }

        // Verify total amount calculation
        val calculatedTotal = subtotal + vat.amount
        require(totalAmount.amount == calculatedTotal.amount) {
            "Total amount must equal subtotal + VAT"
        }

        // Verify subtotal matches line items
        val lineItemsTotal = lineItems.fold(Money.zero(subtotal.currency.currencyCode)) { acc, item ->
            acc + item.totalAmount
        }
        require(subtotal.amount == lineItemsTotal.amount) {
            "Subtotal must equal sum of line items"
        }
    }

    fun isPending(): Boolean = status == InvoiceStatus.PENDING

    fun isPaid(): Boolean = status == InvoiceStatus.PAID

    fun isOverdue(): Boolean =
        status == InvoiceStatus.OVERDUE ||
        (dueDate != null && dueDate.isBefore(LocalDate.now()) && status == InvoiceStatus.PENDING)

    fun isCancelled(): Boolean = status == InvoiceStatus.CANCELLED

    fun isRefunded(): Boolean = status == InvoiceStatus.REFUNDED

    fun isZATCAPending(): Boolean = zatcaStatus == ZATCAStatus.PENDING

    fun isZATCACleared(): Boolean = zatcaStatus == ZATCAStatus.CLEARED

    fun isZATCAFailed(): Boolean = zatcaStatus == ZATCAStatus.FAILED

    fun markAsPaid(): Invoice {
        require(isPending() || isOverdue()) {
            "Only pending or overdue invoices can be marked as paid"
        }
        return copy(
            status = InvoiceStatus.PAID,
            updatedAt = Instant.now()
        )
    }

    fun markAsOverdue(): Invoice {
        require(isPending()) { "Only pending invoices can be marked as overdue" }
        return copy(
            status = InvoiceStatus.OVERDUE,
            updatedAt = Instant.now()
        )
    }

    fun cancel(reason: String?): Invoice {
        require(!isPaid() && !isCancelled()) {
            "Only unpaid invoices can be cancelled"
        }
        return copy(
            status = InvoiceStatus.CANCELLED,
            notes = if (reason != null) "${notes ?: ""}\nCancellation reason: $reason" else notes,
            updatedAt = Instant.now()
        )
    }

    fun markAsRefunded(): Invoice {
        require(isPaid()) { "Only paid invoices can be refunded" }
        return copy(
            status = InvoiceStatus.REFUNDED,
            updatedAt = Instant.now()
        )
    }

    fun submitToZATCA(): Invoice {
        require(isZATCAPending() || isZATCAFailed()) {
            "Only pending or failed invoices can be submitted to ZATCA"
        }
        return copy(
            zatcaStatus = ZATCAStatus.SUBMITTED,
            zatcaSubmittedAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    fun markZATCACleared(clearanceUUID: String): Invoice {
        require(zatcaStatus == ZATCAStatus.SUBMITTED) {
            "Only submitted invoices can be marked as cleared"
        }
        return copy(
            zatcaStatus = ZATCAStatus.CLEARED,
            zatcaClearanceUUID = clearanceUUID,
            zatcaClearedAt = Instant.now(),
            zatcaErrorMessage = null,
            updatedAt = Instant.now()
        )
    }

    fun markZATCAFailed(errorMessage: String): Invoice {
        require(zatcaStatus == ZATCAStatus.SUBMITTED) {
            "Only submitted invoices can be marked as failed"
        }
        return copy(
            zatcaStatus = ZATCAStatus.FAILED,
            zatcaErrorMessage = errorMessage,
            updatedAt = Instant.now()
        )
    }

    fun updateFilePaths(xmlPath: String?, pdfPath: String?): Invoice {
        return copy(
            xmlFilePath = xmlPath,
            pdfFilePath = pdfPath,
            updatedAt = Instant.now()
        )
    }

    fun addQRCode(qrCodeData: String): Invoice {
        return copy(
            qrCode = qrCodeData,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun create(
            organizationId: UUID,
            branchId: UUID,
            memberId: UUID,
            sellerName: String,
            sellerNameArabic: String?,
            sellerVatRegistrationNumber: String,
            sellerAddress: String,
            sellerAddressArabic: String?,
            buyerName: String,
            buyerNameArabic: String?,
            buyerNationalId: String?,
            buyerVatNumber: String?,
            buyerAddress: String?,
            lineItems: List<InvoiceLineItem>,
            dueDate: LocalDate?,
            notes: String?
        ): Invoice {
            val now = Instant.now()
            val issueDate = LocalDate.now()

            // Calculate subtotal from line items
            val subtotal = lineItems.fold(Money.zero("SAR")) { acc, item ->
                acc + item.totalAmount
            }

            // Calculate VAT (15% for Saudi Arabia)
            val vat = VAT.calculateSaudiVAT(subtotal)

            // Calculate total amount
            val totalAmount = subtotal + vat.amount

            // Generate invoice number
            val invoiceNumber = generateInvoiceNumber()

            return Invoice(
                id = UUID.randomUUID(),
                invoiceNumber = invoiceNumber,
                organizationId = organizationId,
                branchId = branchId,
                memberId = memberId,
                sellerName = sellerName,
                sellerNameArabic = sellerNameArabic,
                sellerVatRegistrationNumber = sellerVatRegistrationNumber,
                sellerAddress = sellerAddress,
                sellerAddressArabic = sellerAddressArabic,
                buyerName = buyerName,
                buyerNameArabic = buyerNameArabic,
                buyerNationalId = buyerNationalId,
                buyerVatNumber = buyerVatNumber,
                buyerAddress = buyerAddress,
                lineItems = lineItems,
                subtotal = subtotal,
                vat = vat,
                totalAmount = totalAmount,
                issueDate = issueDate,
                dueDate = dueDate,
                notes = notes,
                qrCode = null,
                zatcaClearanceUUID = null,
                zatcaStatus = ZATCAStatus.PENDING,
                zatcaSubmittedAt = null,
                zatcaClearedAt = null,
                zatcaErrorMessage = null,
                xmlFilePath = null,
                pdfFilePath = null,
                status = InvoiceStatus.PENDING,
                createdAt = now,
                updatedAt = now
            )
        }

        /**
         * Generates a sequential invoice number.
         * Format: INV-YYYYMMDD-NNNNNN
         *
         * Note: In production, this should be generated from a database sequence
         * to ensure uniqueness across distributed systems.
         */
        fun generateInvoiceNumber(): String {
            val timestamp = Instant.now().toEpochMilli()
            val date = LocalDate.now().toString().replace("-", "")
            val random = (100000..999999).random()
            return "INV-$date-$random"
        }
    }
}

/**
 * Invoice status enumeration
 */
enum class InvoiceStatus {
    PENDING,
    PAID,
    OVERDUE,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}

/**
 * ZATCA submission status enumeration
 */
enum class ZATCAStatus {
    PENDING,      // Not yet submitted to ZATCA
    SUBMITTED,    // Submitted to ZATCA, awaiting clearance
    CLEARED,      // Cleared by ZATCA
    FAILED        // ZATCA clearance failed
}
