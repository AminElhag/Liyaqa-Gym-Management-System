package com.liyaqa.gym.application.financial.dto

import com.liyaqa.gym.domain.entities.Refund
import org.springframework.stereotype.Component

/**
 * Mapper for converting between Refund entity and RefundDTO.
 */
@Component
class RefundMapper {

    /**
     * Converts a Refund entity to RefundDTO.
     *
     * @param refund The refund entity
     * @return The refund DTO
     */
    fun toDTO(refund: Refund): RefundDTO {
        return RefundDTO(
            id = refund.id,
            paymentId = refund.paymentId,
            invoiceId = refund.invoiceId,
            memberId = refund.memberId,
            organizationId = refund.organizationId,
            branchId = refund.branchId,
            amount = refund.amount.amount,
            currency = refund.amount.currency.currencyCode,
            reason = refund.reason,
            status = refund.status,
            paymentGatewayRefundId = refund.paymentGatewayRefundId,
            creditNoteNumber = refund.creditNoteNumber,
            creditNoteFilePath = refund.creditNoteFilePath,
            processedAt = refund.processedAt,
            failedAt = refund.failedAt,
            failureReason = refund.failureReason,
            createdAt = refund.createdAt,
            updatedAt = refund.updatedAt
        )
    }

    /**
     * Converts a list of Refund entities to a list of RefundDTOs.
     *
     * @param refunds The list of refund entities
     * @return The list of refund DTOs
     */
    fun toDTOList(refunds: List<Refund>): List<RefundDTO> {
        return refunds.map { toDTO(it) }
    }
}
