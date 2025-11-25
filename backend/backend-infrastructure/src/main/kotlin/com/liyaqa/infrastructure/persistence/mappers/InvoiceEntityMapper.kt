package com.liyaqa.infrastructure.persistence.mappers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.liyaqa.gym.domain.entities.Invoice
import com.liyaqa.gym.domain.valueobjects.InvoiceLineItem
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.gym.domain.valueobjects.VAT
import com.liyaqa.infrastructure.persistence.entities.InvoiceJpaEntity
import com.liyaqa.infrastructure.persistence.entities.MoneyEmbeddable
import com.liyaqa.infrastructure.persistence.entities.VATEmbeddable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.Currency

/**
 * Mapper between Invoice domain entity and InvoiceJpaEntity.
 */
@Component
class InvoiceEntityMapper(
    private val objectMapper: ObjectMapper
) {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Invoice): InvoiceJpaEntity {
        return InvoiceJpaEntity(
            id = domain.id,
            invoiceNumber = domain.invoiceNumber,
            organizationId = domain.organizationId,
            branchId = domain.branchId,
            memberId = domain.memberId,
            sellerName = domain.sellerName,
            sellerNameArabic = domain.sellerNameArabic,
            sellerVatRegistrationNumber = domain.sellerVatRegistrationNumber,
            sellerAddress = domain.sellerAddress,
            sellerAddressArabic = domain.sellerAddressArabic,
            buyerName = domain.buyerName,
            buyerNameArabic = domain.buyerNameArabic,
            buyerNationalId = domain.buyerNationalId,
            buyerVatNumber = domain.buyerVatNumber,
            buyerAddress = domain.buyerAddress,
            lineItems = serializeLineItems(domain.lineItems),
            subtotal = toMoneyEmbeddable(domain.subtotal),
            vat = toVATEmbeddable(domain.vat),
            totalAmount = toMoneyEmbeddable(domain.totalAmount),
            issueDate = domain.issueDate,
            dueDate = domain.dueDate,
            notes = domain.notes,
            qrCode = domain.qrCode,
            zatcaClearanceUUID = domain.zatcaClearanceUUID,
            zatcaStatus = domain.zatcaStatus,
            zatcaSubmittedAt = domain.zatcaSubmittedAt,
            zatcaClearedAt = domain.zatcaClearedAt,
            zatcaErrorMessage = domain.zatcaErrorMessage,
            xmlFilePath = domain.xmlFilePath,
            pdfFilePath = domain.pdfFilePath,
            status = domain.status,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: InvoiceJpaEntity): Invoice {
        return Invoice(
            id = entity.id,
            invoiceNumber = entity.invoiceNumber,
            organizationId = entity.organizationId,
            branchId = entity.branchId,
            memberId = entity.memberId,
            sellerName = entity.sellerName,
            sellerNameArabic = entity.sellerNameArabic,
            sellerVatRegistrationNumber = entity.sellerVatRegistrationNumber,
            sellerAddress = entity.sellerAddress,
            sellerAddressArabic = entity.sellerAddressArabic,
            buyerName = entity.buyerName,
            buyerNameArabic = entity.buyerNameArabic,
            buyerNationalId = entity.buyerNationalId,
            buyerVatNumber = entity.buyerVatNumber,
            buyerAddress = entity.buyerAddress,
            lineItems = deserializeLineItems(entity.lineItems),
            subtotal = toDomainMoney(entity.subtotal),
            vat = toDomainVAT(entity.vat),
            totalAmount = toDomainMoney(entity.totalAmount),
            issueDate = entity.issueDate,
            dueDate = entity.dueDate,
            notes = entity.notes,
            qrCode = entity.qrCode,
            zatcaClearanceUUID = entity.zatcaClearanceUUID,
            zatcaStatus = entity.zatcaStatus,
            zatcaSubmittedAt = entity.zatcaSubmittedAt,
            zatcaClearedAt = entity.zatcaClearedAt,
            zatcaErrorMessage = entity.zatcaErrorMessage,
            xmlFilePath = entity.xmlFilePath,
            pdfFilePath = entity.pdfFilePath,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toMoneyEmbeddable(money: Money): MoneyEmbeddable {
        return MoneyEmbeddable(
            amount = money.amount,
            currency = money.currency.currencyCode
        )
    }

    private fun toDomainMoney(embeddable: MoneyEmbeddable): Money {
        return Money(
            amount = embeddable.amount,
            currency = Currency.getInstance(embeddable.currency)
        )
    }

    private fun toVATEmbeddable(vat: VAT): VATEmbeddable {
        return VATEmbeddable(
            rate = vat.rate,
            vatAmount = vat.amount.amount,
            currency = vat.amount.currency.currencyCode
        )
    }

    private fun toDomainVAT(embeddable: VATEmbeddable): VAT {
        val money = Money(
            amount = embeddable.vatAmount,
            currency = Currency.getInstance(embeddable.currency)
        )
        return VAT(
            rate = embeddable.rate,
            amount = money
        )
    }

    /**
     * Serialize line items to JSON string.
     */
    private fun serializeLineItems(lineItems: List<InvoiceLineItem>): String {
        val dtos = lineItems.map { lineItem ->
            InvoiceLineItemDTO(
                description = lineItem.description,
                descriptionArabic = lineItem.descriptionArabic,
                quantity = lineItem.quantity,
                unitPriceAmount = lineItem.unitPrice.amount,
                unitPriceCurrency = lineItem.unitPrice.currency.currencyCode,
                totalAmount = lineItem.totalAmount.amount,
                totalCurrency = lineItem.totalAmount.currency.currencyCode
            )
        }
        return objectMapper.writeValueAsString(dtos)
    }

    /**
     * Deserialize line items from JSON string.
     */
    private fun deserializeLineItems(json: String): List<InvoiceLineItem> {
        val dtos: List<InvoiceLineItemDTO> = objectMapper.readValue(json)
        return dtos.map { dto ->
            InvoiceLineItem(
                description = dto.description,
                descriptionArabic = dto.descriptionArabic,
                quantity = dto.quantity,
                unitPrice = Money(dto.unitPriceAmount, Currency.getInstance(dto.unitPriceCurrency)),
                totalAmount = Money(dto.totalAmount, Currency.getInstance(dto.totalCurrency))
            )
        }
    }

    /**
     * DTO for JSON serialization of line items.
     */
    private data class InvoiceLineItemDTO(
        val description: String,
        val descriptionArabic: String?,
        val quantity: BigDecimal,
        val unitPriceAmount: BigDecimal,
        val unitPriceCurrency: String,
        val totalAmount: BigDecimal,
        val totalCurrency: String
    )
}
