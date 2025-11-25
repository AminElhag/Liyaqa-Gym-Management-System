package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.Gender
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for Zone.
 * Represents restricted areas within a branch.
 * Examples: Cardio Zone, Weight Training Zone, Pool, Spa, VIP Area.
 */
@Entity
@Table(
    name = "zones",
    indexes = [
        Index(name = "idx_zone_branch_id", columnList = "branch_id"),
        Index(name = "idx_zone_name", columnList = "name"),
        Index(name = "idx_zone_is_active", columnList = "is_active")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class ZoneJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_features", columnDefinition = "jsonb")
    var requiredFeatures: List<String> = emptyList(),

    @Column(name = "capacity")
    var capacity: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_restriction", length = 10)
    var genderRestriction: Gender? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

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
        name = ""
    )
}
