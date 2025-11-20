package com.liyaqa.gym.presentation.dto.invoice

import com.liyaqa.gym.application.financial.dto.InvoiceDTO
import com.liyaqa.gym.application.financial.dto.InvoiceLineItemDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Response DTO for invoice details
 * Delegates to the application layer InvoiceDTO
 */
typealias InvoiceResponse = InvoiceDTO

/**
 * Response for invoice line item
 * Delegates to the application layer InvoiceLineItemDTO
 */
typealias InvoiceLineItemResponse = InvoiceLineItemDTO

/**
 * Summary response for invoice lists
 */
@Schema(description = "Invoice summary for list views")
data class InvoiceSummaryResponse(
    @Schema(description = "Invoice ID")
    val id: UUID,

    @Schema(description = "Invoice number")
    val invoiceNumber: String,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Total amount (including VAT)")
    val totalAmount: BigDecimal,

    @Schema(description = "Currency code")
    val currency: String,

    @Schema(description = "Issue date")
    val issueDate: LocalDate,

    @Schema(description = "Invoice status")
    val status: String,

    @Schema(description = "ZATCA status")
    val zatcaStatus: String,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant
)

/**
 * Response for QR code image
 */
@Schema(description = "ZATCA QR code response")
data class QRCodeResponse(
    @Schema(description = "Invoice ID")
    val invoiceId: UUID,

    @Schema(description = "Invoice number")
    val invoiceNumber: String,

    @Schema(description = "QR code data (Base64 encoded)")
    val qrCode: String,

    @Schema(description = "QR code format", example = "BASE64")
    val format: String = "BASE64"
)

/**
 * Response for invoice generation
 */
@Schema(description = "Invoice generation confirmation")
data class InvoiceGenerationResponse(
    @Schema(description = "Invoice ID")
    val invoiceId: UUID,

    @Schema(description = "Invoice number")
    val invoiceNumber: String,

    @Schema(description = "Total amount (including VAT)")
    val totalAmount: BigDecimal,

    @Schema(description = "VAT amount")
    val vatAmount: BigDecimal,

    @Schema(description = "Currency code")
    val currency: String,

    @Schema(description = "Issue date")
    val issueDate: LocalDate,

    @Schema(description = "PDF file path")
    val pdfFilePath: String?,

    @Schema(description = "XML file path")
    val xmlFilePath: String?,

    @Schema(description = "QR code (Base64)")
    val qrCode: String?,

    @Schema(description = "Success message")
    val message: String = "Invoice generated successfully"
)
