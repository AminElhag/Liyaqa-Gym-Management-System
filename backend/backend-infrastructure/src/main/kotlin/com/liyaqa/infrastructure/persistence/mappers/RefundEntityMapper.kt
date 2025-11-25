package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Refund
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.infrastructure.persistence.entities.MoneyEmbeddable
import com.liyaqa.infrastructure.persistence.entities.RefundJpaEntity
import org.springframework.stereotype.Component
import java.util.Currency

/**
 * Mapper between Refund domain entity and RefundJpaEntity.
 */
@Component
class RefundEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Refund): RefundJpaEntity {
        return RefundJpaEntity(
            id = domain.id,
            paymentId = domain.paymentId,
            invoiceId = domain.invoiceId,
            memberId = domain.memberId,
            organizationId = domain.organizationId,
            branchId = domain.branchId,
            amount = toMoneyEmbeddable(domain.amount),
            reason = domain.reason,
            status = domain.status,
            paymentGatewayRefundId = domain.paymentGatewayRefundId,
            paymentGatewayResponse = domain.paymentGatewayResponse,
            creditNoteNumber = domain.creditNoteNumber,
            creditNoteFilePath = domain.creditNoteFilePath,
            processedAt = domain.processedAt,
            failedAt = domain.failedAt,
            failureReason = domain.failureReason,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: RefundJpaEntity): Refund {
        return Refund(
            id = entity.id,
            paymentId = entity.paymentId,
            invoiceId = entity.invoiceId,
            memberId = entity.memberId,
            organizationId = entity.organizationId,
            branchId = entity.branchId,
            amount = toDomainMoney(entity.amount),
            reason = entity.reason,
            status = entity.status,
            paymentGatewayRefundId = entity.paymentGatewayRefundId,
            paymentGatewayResponse = entity.paymentGatewayResponse,
            creditNoteNumber = entity.creditNoteNumber,
            creditNoteFilePath = entity.creditNoteFilePath,
            processedAt = entity.processedAt,
            failedAt = entity.failedAt,
            failureReason = entity.failureReason,
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
}
