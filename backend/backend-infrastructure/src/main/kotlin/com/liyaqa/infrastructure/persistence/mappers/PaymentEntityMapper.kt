package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Payment
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.gym.domain.valueobjects.VAT
import com.liyaqa.infrastructure.persistence.entities.MoneyEmbeddable
import com.liyaqa.infrastructure.persistence.entities.PaymentJpaEntity
import com.liyaqa.infrastructure.persistence.entities.VATEmbeddable
import org.springframework.stereotype.Component
import java.util.Currency

/**
 * Mapper between Payment domain entity and PaymentJpaEntity.
 */
@Component
class PaymentEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Payment): PaymentJpaEntity {
        return PaymentJpaEntity(
            id = domain.id,
            tenantId = domain.tenantId,
            memberId = domain.memberId,
            organizationId = domain.organizationId,
            branchId = domain.branchId,
            amount = toMoneyEmbeddable(domain.amount),
            vat = toVATEmbeddable(domain.vat),
            totalAmount = toMoneyEmbeddable(domain.totalAmount),
            method = domain.method,
            status = domain.status,
            invoiceNumber = domain.invoiceNumber,
            referenceNumber = domain.referenceNumber,
            subscriptionId = domain.subscriptionId,
            ptSessionId = domain.ptSessionId,
            description = domain.description,
            paymentGatewayId = domain.paymentGatewayId,
            paymentGatewayResponse = domain.paymentGatewayResponse,
            paidAt = domain.paidAt,
            refundedAt = domain.refundedAt,
            refundAmount = domain.refundAmount?.let { toMoneyEmbeddable(it) },
            refundReason = domain.refundReason,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: PaymentJpaEntity): Payment {
        return Payment(
            id = entity.id,
            tenantId = entity.tenantId,
            memberId = entity.memberId,
            organizationId = entity.organizationId,
            branchId = entity.branchId,
            amount = toDomainMoney(entity.amount),
            vat = toDomainVAT(entity.vat),
            totalAmount = toDomainMoney(entity.totalAmount),
            method = entity.method,
            status = entity.status,
            invoiceNumber = entity.invoiceNumber,
            referenceNumber = entity.referenceNumber,
            subscriptionId = entity.subscriptionId,
            ptSessionId = entity.ptSessionId,
            description = entity.description,
            paymentGatewayId = entity.paymentGatewayId,
            paymentGatewayResponse = entity.paymentGatewayResponse,
            paidAt = entity.paidAt,
            refundedAt = entity.refundedAt,
            refundAmount = entity.refundAmount?.let { toDomainMoney(it) },
            refundReason = entity.refundReason,
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
}
