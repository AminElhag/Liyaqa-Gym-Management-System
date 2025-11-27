package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.FacilityType
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for Branch.
 * Represents a physical gym location belonging to an Organization.
 */
@Entity
@Table(
    name = "branches",
    indexes = [
        Index(name = "idx_branch_organization_id", columnList = "organization_id"),
        Index(name = "idx_branch_name", columnList = "name"),
        Index(name = "idx_branch_is_active", columnList = "is_active")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class BranchJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Embedded
    var address: AddressEmbeddable,

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", nullable = false, length = 50)
    var facilityType: FacilityType,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

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
        organizationId = UUID.randomUUID(),
        name = "",
        address = AddressEmbeddable(),
        facilityType = FacilityType.FAMILY
    )
}

/**
 * Embeddable for Address value object.
 */
@Embeddable
data class AddressEmbeddable(
    @Column(name = "street", nullable = false, length = 500)
    var street: String = "",

    @Column(name = "city", nullable = false, length = 100)
    var city: String = "",

    @Column(name = "state", nullable = false, length = 100)
    var state: String = "",

    @Column(name = "country", nullable = false, length = 100)
    var country: String = "Saudi Arabia",

    @Column(name = "postal_code", nullable = false, length = 20)
    var postalCode: String = ""
)
