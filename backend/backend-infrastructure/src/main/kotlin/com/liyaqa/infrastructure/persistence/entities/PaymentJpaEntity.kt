package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.PaymentMethod
import com.liyaqa.gym.domain.entities.PaymentStatus
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for Payment.
 * Represents a financial transaction linked to various payment contexts.
 */
@Entity
@Table(
    name = "payments",
    indexes = [
        Index(name = "idx_payment_member_id", columnList = "member_id"),
        Index(name = "idx_payment_organization_id", columnList = "organization_id"),
        Index(name = "idx_payment_branch_id", columnList = "branch_id"),
        Index(name = "idx_payment_status", columnList = "status"),
        Index(name = "idx_payment_method", columnList = "method"),
        Index(name = "idx_payment_invoice_number", columnList = "invoice_number"),
        Index(name = "idx_payment_subscription_id", columnList = "subscription_id"),
        Index(name = "idx_payment_pt_session_id", columnList = "pt_session_id"),
        Index(name = "idx_payment_paid_at", columnList = "paid_at")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class PaymentJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

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

    @Embedded
    var vat: VATEmbeddable,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_amount")),
        AttributeOverride(name = "currency", column = Column(name = "total_currency"))
    )
    var totalAmount: MoneyEmbeddable,

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 50)
    var method: PaymentMethod,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: PaymentStatus,

    @Column(name = "invoice_number", nullable = false, length = 100, unique = true)
    var invoiceNumber: String,

    @Column(name = "reference_number", length = 100)
    var referenceNumber: String? = null,

    @Column(name = "subscription_id")
    var subscriptionId: UUID? = null,

    @Column(name = "pt_session_id")
    var ptSessionId: UUID? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "payment_gateway_id", length = 255)
    var paymentGatewayId: String? = null,

    @Column(name = "payment_gateway_response", columnDefinition = "TEXT")
    var paymentGatewayResponse: String? = null,

    @Column(name = "paid_at")
    var paidAt: Instant? = null,

    @Column(name = "refunded_at")
    var refundedAt: Instant? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "refund_amount")),
        AttributeOverride(name = "currency", column = Column(name = "refund_currency"))
    )
    var refundAmount: MoneyEmbeddable? = null,

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    var refundReason: String? = null,

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
        tenantId = UUID.randomUUID(),
        memberId = UUID.randomUUID(),
        organizationId = UUID.randomUUID(),
        branchId = UUID.randomUUID(),
        amount = MoneyEmbeddable(),
        vat = VATEmbeddable(),
        totalAmount = MoneyEmbeddable(),
        method = PaymentMethod.CASH,
        status = PaymentStatus.PENDING,
        invoiceNumber = ""
    )
}

/**
 * Embeddable for VAT value object.
 */
@Embeddable
data class VATEmbeddable(
    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 4)
    var rate: BigDecimal = BigDecimal.ZERO,

    @Column(name = "vat_amount", nullable = false, precision = 19, scale = 2)
    var vatAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "vat_currency", nullable = false, length = 3)
    var currency: String = "SAR"
)
