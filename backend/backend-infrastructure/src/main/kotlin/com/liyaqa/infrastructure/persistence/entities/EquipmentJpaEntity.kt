package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.EquipmentCategory
import com.liyaqa.gym.domain.entities.EquipmentStatus
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * JPA entity for Equipment.
 * Represents gym equipment belonging to a Branch.
 */
@Entity
@Table(
    name = "equipment",
    indexes = [
        Index(name = "idx_equipment_branch_id", columnList = "branch_id"),
        Index(name = "idx_equipment_name", columnList = "name"),
        Index(name = "idx_equipment_category", columnList = "category"),
        Index(name = "idx_equipment_status", columnList = "status"),
        Index(name = "idx_equipment_serial_number", columnList = "serial_number"),
        Index(name = "idx_equipment_qr_code", columnList = "qr_code"),
        Index(name = "idx_equipment_next_maintenance_date", columnList = "next_maintenance_date")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class EquipmentJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "name_arabic", length = 255)
    var nameArabic: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    var category: EquipmentCategory,

    @Column(name = "manufacturer", length = 255)
    var manufacturer: String? = null,

    @Column(name = "model", length = 255)
    var model: String? = null,

    @Column(name = "serial_number", length = 255)
    var serialNumber: String? = null,

    @Column(name = "purchase_date", nullable = false)
    var purchaseDate: LocalDate,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "purchase_price_amount")),
        AttributeOverride(name = "currency", column = Column(name = "purchase_price_currency"))
    )
    var purchasePrice: MoneyEmbeddable? = null,

    @Column(name = "warranty_expiry_date")
    var warrantyExpiryDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: EquipmentStatus,

    @Column(name = "location", length = 255)
    var location: String? = null,

    @Column(name = "qr_code", length = 255)
    var qrCode: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "last_maintenance_date")
    var lastMaintenanceDate: LocalDate? = null,

    @Column(name = "next_maintenance_date")
    var nextMaintenanceDate: LocalDate? = null,

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
        branchId = UUID.randomUUID(),
        name = "",
        category = EquipmentCategory.OTHER,
        purchaseDate = LocalDate.now(),
        status = EquipmentStatus.OPERATIONAL
    )
}
