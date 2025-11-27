package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.entities.MemberStatus
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * JPA entity for Member.
 * Represents a gym member belonging to a Branch.
 */
@Entity
@Table(
    name = "members",
    indexes = [
        Index(name = "idx_member_branch_id", columnList = "branch_id"),
        Index(name = "idx_member_email", columnList = "email"),
        Index(name = "idx_member_phone", columnList = "phone"),
        Index(name = "idx_member_national_id", columnList = "national_id"),
        Index(name = "idx_member_status", columnList = "status"),
        Index(name = "idx_member_name", columnList = "name")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class MemberJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID,

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "name_arabic", length = 255)
    var nameArabic: String? = null,

    @Embedded
    var contactInfo: ContactInfoEmbeddable,

    @Column(name = "national_id", length = 50)
    var nationalId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    var gender: Gender,

    @Column(name = "date_of_birth")
    var dateOfBirth: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: MemberStatus,

    @Column(name = "profile_photo_url", length = 500)
    var profilePhotoUrl: String? = null,

    @Column(name = "emergency_contact_name", length = 255)
    var emergencyContactName: String? = null,

    @Column(name = "emergency_contact_phone", length = 50)
    var emergencyContactPhone: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

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
        branchId = UUID.randomUUID(),
        name = "",
        contactInfo = ContactInfoEmbeddable(),
        gender = Gender.MALE,
        status = MemberStatus.ACTIVE
    )
}

/**
 * Embeddable for ContactInfo value object.
 */
@Embeddable
data class ContactInfoEmbeddable(
    @Column(name = "email", nullable = false, length = 255)
    var email: String = "",

    @Column(name = "phone", nullable = false, length = 50)
    var phone: String = ""
)
