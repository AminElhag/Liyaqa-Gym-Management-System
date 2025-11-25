package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.RefundStatus
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for Refund.
 * Represents a payment refund transaction.
 */
@Entity
@Table(
    name = "refunds",
    indexes = [
        Index(name = "idx_refund_payment_id", columnList = "payment_id"),
        Index(name = "idx_refund_invoice_id", columnList = "invoice_id"),
        Index(name = "idx_refund_member_id", columnList = "member_id"),
        Index(name = "idx_refund_organization_id", columnList = "organization_id"),
        Index(name = "idx_refund_branch_id", columnList = "branch_id"),
        Index(name = "idx_refund_status", columnList = "status"),
        Index(name = "idx_refund_gateway_refund_id", columnList = "payment_gateway_refund_id"),
        Index(name = "idx_refund_credit_note", columnList = "credit_note_number"),
        Index(name = "idx_refund_processed_at", columnList = "processed_at")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class RefundJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "payment_id", nullable = false)
    var paymentId: UUID,

    @Column(name = "invoice_id")
    var invoiceId: UUID? = null,

    @Column(name = "member_id", nullable = false)
    var memberId: UUID,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID,

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "amount")),
        AttributeOverride(name = "currency", column = Column(name = "currency"))
    )
    var amount: MoneyEmbeddable,

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    var reason: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: RefundStatus,

    @Column(name = "payment_gateway_refund_id", length = 255)
    var paymentGatewayRefundId: String? = null,

    @Column(name = "payment_gateway_response", columnDefinition = "TEXT")
    var paymentGatewayResponse: String? = null,

    @Column(name = "credit_note_number", length = 100)
    var creditNoteNumber: String? = null,

    @Column(name = "credit_note_file_path", length = 500)
    var creditNoteFilePath: String? = null,

    @Column(name = "processed_at")
    var processedAt: Instant? = null,

    @Column(name = "failed_at")
    var failedAt: Instant? = null,

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    var failureReason: String? = null,

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    constructor() : this(
        id = UUID.randomUUID(),
        paymentId = UUID.randomUUID(),
        memberId = UUID.randomUUID(),
        organizationId = UUID.randomUUID(),
        branchId = UUID.randomUUID(),
        amount = MoneyEmbeddable(),
        reason = "",
        status = RefundStatus.PENDING
    )
}
