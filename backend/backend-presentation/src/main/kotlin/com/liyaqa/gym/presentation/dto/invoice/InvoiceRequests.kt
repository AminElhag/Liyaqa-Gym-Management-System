package com.liyaqa.gym.presentation.dto.invoice

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Request DTO for generating an invoice
 */
@Schema(description = "Request to generate a ZATCA-compliant invoice")
data class GenerateInvoiceRequest(
    @field:NotNull(message = "Member ID is required")
    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @field:NotNull(message = "Organization ID is required")
    @Schema(description = "Organization ID", example = "123e4567-e89b-12d3-a456-426614174001")
    val organizationId: UUID,

    @field:NotNull(message = "Branch ID is required")
    @Schema(description = "Branch ID", example = "123e4567-e89b-12d3-a456-426614174002")
    val branchId: UUID,

    @field:NotEmpty(message = "Line items are required")
    @field:Valid
    @Schema(description = "Invoice line items")
    val lineItems: List<InvoiceLineItemRequest>,

    @Schema(description = "Due date for payment", example = "2025-12-20")
    val dueDate: LocalDate? = null,

    @field:Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes")
    val notes: String? = null
)

/**
 * Request DTO for invoice line item
 */
@Schema(description = "Invoice line item")
data class InvoiceLineItemRequest(
    @field:NotBlank(message = "Description is required")
    @field:Size(min = 2, max = 200, message = "Description must be between 2 and 200 characters")
    @Schema(description = "Item description in English", example = "Gold Membership - 1 Month")
    val description: String,

    @field:Size(max = 200, message = "Arabic description must not exceed 200 characters")
    @Schema(description = "Item description in Arabic", example = "عضوية ذهبية - شهر واحد")
    val descriptionArabic: String? = null,

    @field:NotNull(message = "Quantity is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be positive")
    @Schema(description = "Item quantity", example = "1")
    val quantity: BigDecimal,

    @field:NotNull(message = "Unit price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    @Schema(description = "Unit price (excluding VAT)", example = "299.99")
    val unitPrice: BigDecimal,

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Schema(description = "Currency code (ISO 4217)", example = "SAR")
    val currency: String = "SAR"
)
