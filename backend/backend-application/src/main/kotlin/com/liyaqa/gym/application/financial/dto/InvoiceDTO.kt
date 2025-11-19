package com.liyaqa.gym.application.financial.dto

import com.liyaqa.gym.domain.entities.InvoiceStatus
import com.liyaqa.gym.domain.entities.ZATCAStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * DTO for invoice data transfer.
 */
data class InvoiceDTO(
    val id: UUID,
    val invoiceNumber: String,
    val organizationId: UUID,
    val branchId: UUID,
    val memberId: UUID,

    // Seller information
    val sellerName: String,
    val sellerNameArabic: String?,
    val sellerVatRegistrationNumber: String,
    val sellerAddress: String,
    val sellerAddressArabic: String?,

    // Buyer information
    val buyerName: String,
    val buyerNameArabic: String?,
    val buyerNationalId: String?,
    val buyerVatNumber: String?,
    val buyerAddress: String?,

    // Invoice details
    val lineItems: List<InvoiceLineItemDTO>,
    val subtotal: BigDecimal,
    val vatRate: BigDecimal,
    val vatAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val currency: String,
    val issueDate: LocalDate,
    val dueDate: LocalDate?,
    val notes: String?,

    // ZATCA compliance
    val qrCode: String?,
    val zatcaClearanceUUID: String?,
    val zatcaStatus: ZATCAStatus,
    val zatcaSubmittedAt: Instant?,
    val zatcaClearedAt: Instant?,
    val zatcaErrorMessage: String?,

    // Files
    val xmlFilePath: String?,
    val pdfFilePath: String?,

    // Status
    val status: InvoiceStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * DTO for invoice line item.
 */
data class InvoiceLineItemDTO(
    val description: String,
    val descriptionArabic: String?,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val totalAmount: BigDecimal,
    val currency: String
)
