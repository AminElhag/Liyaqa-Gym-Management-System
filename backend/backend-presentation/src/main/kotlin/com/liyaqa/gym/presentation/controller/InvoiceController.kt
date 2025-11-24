package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.application.financial.GenerateInvoiceUseCase
import com.liyaqa.gym.application.financial.commands.GenerateInvoiceCommand
import com.liyaqa.gym.application.financial.commands.InvoiceLineItemCommand
import com.liyaqa.gym.application.financial.dto.InvoiceDTO
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.repositories.InvoiceRepository
import com.liyaqa.gym.presentation.dto.common.ApiResponse
import com.liyaqa.gym.presentation.dto.invoice.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.Base64
import java.util.UUID

/**
 * REST Controller for invoice management operations.
 * Handles invoice generation, retrieval, PDF generation, and ZATCA QR codes.
 */
@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoices", description = "Invoice management and ZATCA compliance endpoints")
class InvoiceController(
    private val generateInvoiceUseCase: GenerateInvoiceUseCase,
    private val invoiceRepository: InvoiceRepository
) {

    private val logger = LoggerFactory.getLogger(InvoiceController::class.java)

    /**
     * Get invoice details by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get invoice details",
        description = "Retrieve detailed information about a specific invoice"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Invoice retrieved successfully",
                content = [Content(schema = Schema(implementation = InvoiceResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Invoice not found"
            )
        ]
    )
    fun getInvoiceDetails(
        @Parameter(description = "Invoice ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<InvoiceDTO>> {
        logger.info("Fetching invoice details for ID: $id")

        val invoiceOptional = invoiceRepository.findById(id)
            .getOrThrow()

        if (!invoiceOptional.isPresent) {
            throw ResourceNotFoundException("Invoice not found with ID: $id")
        }

        val invoice = invoiceOptional.get()
        val response = InvoiceDTO(
            id = invoice.id,
            invoiceNumber = invoice.invoiceNumber,
            organizationId = invoice.organizationId,
            branchId = invoice.branchId,
            memberId = invoice.memberId,
            sellerName = invoice.sellerName,
            sellerNameArabic = invoice.sellerNameArabic,
            sellerVatRegistrationNumber = invoice.sellerVatRegistrationNumber,
            sellerAddress = invoice.sellerAddress,
            sellerAddressArabic = invoice.sellerAddressArabic,
            buyerName = invoice.buyerName,
            buyerNameArabic = invoice.buyerNameArabic,
            buyerNationalId = invoice.buyerNationalId,
            buyerVatNumber = invoice.buyerVatNumber,
            buyerAddress = invoice.buyerAddress,
            lineItems = invoice.lineItems.map {
                com.liyaqa.gym.application.financial.dto.InvoiceLineItemDTO(
                    description = it.description,
                    descriptionArabic = it.descriptionArabic,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice.amount,
                    totalAmount = it.totalAmount.amount,
                    currency = it.unitPrice.currency.currencyCode
                )
            },
            subtotal = invoice.subtotal.amount,
            vatRate = invoice.vat.ratePercentage,
            vatAmount = invoice.vat.amount.amount,
            totalAmount = invoice.totalAmount.amount,
            currency = invoice.totalAmount.currency.currencyCode,
            issueDate = invoice.issueDate,
            dueDate = invoice.dueDate,
            notes = invoice.notes,
            qrCode = invoice.qrCode,
            zatcaClearanceUUID = invoice.zatcaClearanceUUID,
            zatcaStatus = invoice.zatcaStatus,
            zatcaSubmittedAt = invoice.zatcaSubmittedAt,
            zatcaClearedAt = invoice.zatcaClearedAt,
            zatcaErrorMessage = invoice.zatcaErrorMessage,
            xmlFilePath = invoice.xmlFilePath,
            pdfFilePath = invoice.pdfFilePath,
            status = invoice.status,
            createdAt = invoice.createdAt,
            updatedAt = invoice.updatedAt
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Download invoice PDF
     */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Download invoice PDF",
        description = "Download the PDF file for a specific invoice"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "PDF file retrieved successfully",
                content = [Content(mediaType = "application/pdf")]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Invoice or PDF not found"
            )
        ]
    )
    fun downloadInvoicePDF(
        @Parameter(description = "Invoice ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<Resource> {
        logger.info("Downloading invoice PDF for ID: $id")

        val invoiceOptional = invoiceRepository.findById(id)
            .getOrThrow()

        if (!invoiceOptional.isPresent) {
            throw ResourceNotFoundException("Invoice not found with ID: $id")
        }

        val invoice = invoiceOptional.get()

        if (invoice.pdfFilePath == null) {
            throw ResourceNotFoundException("PDF not yet generated for invoice: $id")
        }

        // TODO: In production, read actual PDF file from storage
        // For now, return a placeholder
        val pdfContent = "PDF content for invoice ${invoice.invoiceNumber}".toByteArray()
        val resource = ByteArrayResource(pdfContent)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${invoice.invoiceNumber}.pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfContent.size.toLong())
            .body(resource)
    }

    /**
     * Get ZATCA QR code image
     */
    @GetMapping("/{id}/qrcode")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get ZATCA QR code",
        description = "Get the ZATCA-compliant QR code for an invoice"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "QR code retrieved successfully",
                content = [Content(schema = Schema(implementation = QRCodeResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Invoice or QR code not found"
            )
        ]
    )
    fun getInvoiceQRCode(
        @Parameter(description = "Invoice ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<QRCodeResponse>> {
        logger.info("Fetching QR code for invoice ID: $id")

        val invoiceOptional = invoiceRepository.findById(id)
            .getOrThrow()

        if (!invoiceOptional.isPresent) {
            throw ResourceNotFoundException("Invoice not found with ID: $id")
        }

        val invoice = invoiceOptional.get()

        val qrCode = invoice.qrCode
            ?: throw ResourceNotFoundException("QR code not yet generated for invoice: $id")

        val response = QRCodeResponse(
            invoiceId = invoice.id,
            invoiceNumber = invoice.invoiceNumber,
            qrCode = qrCode,
            format = "BASE64"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get member's invoices
     */
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get member's invoices",
        description = "Retrieve all invoices for a specific member"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Invoices retrieved successfully",
                content = [Content(schema = Schema(implementation = InvoiceSummaryResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            )
        ]
    )
    fun getMemberInvoices(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable memberId: UUID,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<InvoiceSummaryResponse>>> {
        logger.info("Fetching invoices for member: $memberId (page: $page, size: $size)")

        val invoices = invoiceRepository.findByMember(memberId, page, size)
            .getOrThrow()

        val response = invoices.map { invoice ->
            InvoiceSummaryResponse(
                id = invoice.id,
                invoiceNumber = invoice.invoiceNumber,
                memberId = invoice.memberId,
                totalAmount = invoice.totalAmount.amount,
                currency = invoice.totalAmount.currency.currencyCode,
                issueDate = invoice.issueDate,
                status = invoice.status.name,
                zatcaStatus = invoice.zatcaStatus.name,
                createdAt = invoice.createdAt
            )
        }

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Generate invoice manually (admin only)
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Generate invoice manually",
        description = "Manually generate a ZATCA-compliant invoice. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Invoice generated successfully",
                content = [Content(schema = Schema(implementation = InvoiceGenerationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Member or branch not found"
            )
        ]
    )
    fun generateInvoice(
        @Valid @RequestBody request: GenerateInvoiceRequest
    ): ResponseEntity<ApiResponse<InvoiceGenerationResponse>> {
        logger.info("Generating invoice for member: ${request.memberId}")

        val command = GenerateInvoiceCommand(
            memberId = request.memberId,
            organizationId = request.organizationId,
            branchId = request.branchId,
            lineItems = request.lineItems.map { lineItem ->
                InvoiceLineItemCommand(
                    description = lineItem.description,
                    descriptionArabic = lineItem.descriptionArabic,
                    quantity = lineItem.quantity,
                    unitPrice = lineItem.unitPrice,
                    currency = lineItem.currency
                )
            },
            dueDate = request.dueDate,
            notes = request.notes
        )

        val invoiceId = generateInvoiceUseCase.execute(command)
            .getOrThrow()

        // Retrieve the generated invoice
        val invoice = invoiceRepository.findById(invoiceId)
            .getOrThrow()
            .orElseThrow { ResourceNotFoundException("Invoice not found after generation") }

        val response = InvoiceGenerationResponse(
            invoiceId = invoice.id,
            invoiceNumber = invoice.invoiceNumber,
            totalAmount = invoice.totalAmount.amount,
            vatAmount = invoice.vat.amount.amount,
            currency = invoice.totalAmount.currency.currencyCode,
            issueDate = invoice.issueDate,
            pdfFilePath = invoice.pdfFilePath,
            xmlFilePath = invoice.xmlFilePath,
            qrCode = invoice.qrCode,
            message = "Invoice generated successfully"
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }
}
