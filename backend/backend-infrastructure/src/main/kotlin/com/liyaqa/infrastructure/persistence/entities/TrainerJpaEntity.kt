package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.ClassType
import com.liyaqa.gym.domain.entities.TrainerStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Where
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * JPA entity for Trainer.
 * Represents a fitness instructor/trainer belonging to a Branch.
 */
@Entity
@Table(
    name = "trainers",
    indexes = [
        Index(name = "idx_trainer_branch_id", columnList = "branch_id"),
        Index(name = "idx_trainer_name", columnList = "name"),
        Index(name = "idx_trainer_status", columnList = "status"),
        Index(name = "idx_trainer_email", columnList = "email")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class TrainerJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "name_arabic", length = 255)
    var nameArabic: String? = null,

    @Embedded
    var contactInfo: ContactInfoEmbeddable,

    @ElementCollection(targetClass = ClassType::class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "trainer_specializations",
        joinColumns = [JoinColumn(name = "trainer_id")]
    )
    @Column(name = "specialization")
    @Enumerated(EnumType.STRING)
    var specializations: MutableSet<ClassType> = mutableSetOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "certifications", columnDefinition = "jsonb")
    var certifications: List<CertificationEmbeddable> = emptyList(),

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "hourly_rate_amount")),
        AttributeOverride(name = "currency", column = Column(name = "hourly_rate_currency"))
    )
    var hourlyRate: MoneyEmbeddable,

    @Column(name = "biography", columnDefinition = "TEXT")
    var biography: String? = null,

    @Column(name = "photo_url", length = 500)
    var photoUrl: String? = null,

    @Column(name = "hire_date", nullable = false)
    var hireDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: TrainerStatus,

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
        branchId = UUID.randomUUID(),
        name = "",
        contactInfo = ContactInfoEmbeddable(),
        hourlyRate = MoneyEmbeddable(),
        hireDate = LocalDate.now(),
        status = TrainerStatus.ACTIVE
    )
}

/**
 * Embeddable for Certification.
 */
@Embeddable
data class CertificationEmbeddable(
    var name: String = "",
    var issuingOrganization: String = "",
    var issueDate: LocalDate = LocalDate.now(),
    var expiryDate: LocalDate? = null,
    var certificateNumber: String? = null
)
