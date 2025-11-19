package com.liyaqa.gym.application.financial.dto

import com.liyaqa.gym.domain.entities.RefundStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * DTO for refund data transfer.
 */
data class RefundDTO(
    val id: UUID,
    val paymentId: UUID,
    val invoiceId: UUID?,
    val memberId: UUID,
    val organizationId: UUID,
    val branchId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val reason: String,
    val status: RefundStatus,
    val paymentGatewayRefundId: String?,
    val creditNoteNumber: String?,
    val creditNoteFilePath: String?,
    val processedAt: Instant?,
    val failedAt: Instant?,
    val failureReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
