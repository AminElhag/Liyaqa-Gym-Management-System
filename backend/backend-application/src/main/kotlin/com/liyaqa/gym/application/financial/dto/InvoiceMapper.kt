package com.liyaqa.gym.application.financial.dto

import com.liyaqa.gym.domain.entities.Invoice
import com.liyaqa.gym.domain.valueobjects.InvoiceLineItem
import org.springframework.stereotype.Component

/**
 * Mapper for converting between Invoice entity and InvoiceDTO.
 */
@Component
class InvoiceMapper {

    /**
     * Converts an Invoice entity to InvoiceDTO.
     *
     * @param invoice The invoice entity
     * @return The invoice DTO
     */
    fun toDTO(invoice: Invoice): InvoiceDTO {
        return InvoiceDTO(
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
            lineItems = invoice.lineItems.map { toLineItemDTO(it) },
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
    }

    /**
     * Converts an InvoiceLineItem value object to InvoiceLineItemDTO.
     *
     * @param lineItem The line item value object
     * @return The line item DTO
     */
    fun toLineItemDTO(lineItem: InvoiceLineItem): InvoiceLineItemDTO {
        return InvoiceLineItemDTO(
            description = lineItem.description,
            descriptionArabic = lineItem.descriptionArabic,
            quantity = lineItem.quantity,
            unitPrice = lineItem.unitPrice.amount,
            totalAmount = lineItem.totalAmount.amount,
            currency = lineItem.totalAmount.currency.currencyCode
        )
    }

    /**
     * Converts a list of Invoice entities to a list of InvoiceDTOs.
     *
     * @param invoices The list of invoice entities
     * @return The list of invoice DTOs
     */
    fun toDTOList(invoices: List<Invoice>): List<InvoiceDTO> {
        return invoices.map { toDTO(it) }
    }
}
