package com.liyaqa.gym.application.financial.commands

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Command for generating a ZATCA-compliant invoice.
 *
 * @property memberId The member to invoice
 * @property organizationId The organization identifier
 * @property branchId The branch identifier
 * @property lineItems List of line items for the invoice
 * @property dueDate Optional due date for payment
 * @property notes Optional notes for the invoice
 */
data class GenerateInvoiceCommand(
    val memberId: UUID,
    val organizationId: UUID,
    val branchId: UUID,
    val lineItems: List<InvoiceLineItemCommand>,
    val dueDate: LocalDate? = null,
    val notes: String? = null
)

/**
 * Line item for an invoice.
 *
 * @property description Item description in English
 * @property descriptionArabic Item description in Arabic (for ZATCA compliance)
 * @property quantity Item quantity
 * @property unitPrice Unit price (excluding VAT)
 * @property currency Currency code (e.g., "SAR")
 */
data class InvoiceLineItemCommand(
    val description: String,
    val descriptionArabic: String?,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val currency: String = "SAR"
)
