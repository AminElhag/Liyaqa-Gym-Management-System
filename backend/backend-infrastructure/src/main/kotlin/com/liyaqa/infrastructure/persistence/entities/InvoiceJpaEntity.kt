package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.InvoiceStatus
import com.liyaqa.gym.domain.entities.ZATCAStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Where
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * JPA entity for Invoice.
 * Represents a ZATCA-compliant tax invoice.
 */
@Entity
@Table(
    name = "invoices",
    indexes = [
        Index(name = "idx_invoice_number", columnList = "invoice_number", unique = true),
        Index(name = "idx_invoice_member_id", columnList = "member_id"),
        Index(name = "idx_invoice_organization_id", columnList = "organization_id"),
        Index(name = "idx_invoice_branch_id", columnList = "branch_id"),
        Index(name = "idx_invoice_status", columnList = "status"),
        Index(name = "idx_invoice_zatca_status", columnList = "zatca_status"),
        Index(name = "idx_invoice_issue_date", columnList = "issue_date"),
        Index(name = "idx_invoice_due_date", columnList = "due_date")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class InvoiceJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "invoice_number", nullable = false, length = 100, unique = true)
    var invoiceNumber: String,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID,

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Column(name = "member_id", nullable = false)
    var memberId: UUID,

    // Seller information
    @Column(name = "seller_name", nullable = false, length = 255)
    var sellerName: String,

    @Column(name = "seller_name_arabic", length = 255)
    var sellerNameArabic: String? = null,

    @Column(name = "seller_vat_registration_number", nullable = false, length = 50)
    var sellerVatRegistrationNumber: String,

    @Column(name = "seller_address", columnDefinition = "TEXT", nullable = false)
    var sellerAddress: String,

    @Column(name = "seller_address_arabic", columnDefinition = "TEXT")
    var sellerAddressArabic: String? = null,

    // Buyer information
    @Column(name = "buyer_name", nullable = false, length = 255)
    var buyerName: String,

    @Column(name = "buyer_name_arabic", length = 255)
    var buyerNameArabic: String? = null,

    @Column(name = "buyer_national_id", length = 50)
    var buyerNationalId: String? = null,

    @Column(name = "buyer_vat_number", length = 50)
    var buyerVatNumber: String? = null,

    @Column(name = "buyer_address", columnDefinition = "TEXT")
    var buyerAddress: String? = null,

    // Invoice details
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "line_items", columnDefinition = "jsonb", nullable = false)
    var lineItems: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "subtotal_amount")),
        AttributeOverride(name = "currency", column = Column(name = "subtotal_currency"))
    )
    var subtotal: MoneyEmbeddable,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "rate", column = Column(name = "vat_rate")),
        AttributeOverride(name = "vatAmount", column = Column(name = "vat_amount")),
        AttributeOverride(name = "currency", column = Column(name = "vat_currency"))
    )
    var vat: VATEmbeddable,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_amount")),
        AttributeOverride(name = "currency", column = Column(name = "total_currency"))
    )
    var totalAmount: MoneyEmbeddable,

    @Column(name = "issue_date", nullable = false)
    var issueDate: LocalDate,

    @Column(name = "due_date")
    var dueDate: LocalDate? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    // ZATCA compliance fields
    @Column(name = "qr_code", columnDefinition = "TEXT")
    var qrCode: String? = null,

    @Column(name = "zatca_clearance_uuid", length = 255)
    var zatcaClearanceUUID: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "zatca_status", nullable = false, length = 50)
    var zatcaStatus: ZATCAStatus,

    @Column(name = "zatca_submitted_at")
    var zatcaSubmittedAt: Instant? = null,

    @Column(name = "zatca_cleared_at")
    var zatcaClearedAt: Instant? = null,

    @Column(name = "zatca_error_message", columnDefinition = "TEXT")
    var zatcaErrorMessage: String? = null,

    // Storage paths
    @Column(name = "xml_file_path", length = 500)
    var xmlFilePath: String? = null,

    @Column(name = "pdf_file_path", length = 500)
    var pdfFilePath: String? = null,

    // Audit fields
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: InvoiceStatus,

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
        invoiceNumber = "",
        organizationId = UUID.randomUUID(),
        branchId = UUID.randomUUID(),
        memberId = UUID.randomUUID(),
        sellerName = "",
        sellerVatRegistrationNumber = "",
        sellerAddress = "",
        buyerName = "",
        lineItems = "[]",
        subtotal = MoneyEmbeddable(),
        vat = VATEmbeddable(),
        totalAmount = MoneyEmbeddable(),
        issueDate = LocalDate.now(),
        zatcaStatus = ZATCAStatus.PENDING,
        status = InvoiceStatus.PENDING
    )
}
