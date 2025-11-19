package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.ClassLevel
import com.liyaqa.gym.domain.entities.ClassType
import com.liyaqa.gym.domain.entities.Gender
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for Class.
 * Represents a fitness class offering belonging to a Branch.
 */
@Entity
@Table(
    name = "classes",
    indexes = [
        Index(name = "idx_class_branch_id", columnList = "branch_id"),
        Index(name = "idx_class_name", columnList = "name"),
        Index(name = "idx_class_type", columnList = "type"),
        Index(name = "idx_class_level", columnList = "level"),
        Index(name = "idx_class_is_active", columnList = "is_active")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class ClassJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "branch_id", nullable = false)
    var branchId: UUID,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "name_arabic", length = 255)
    var nameArabic: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    var type: ClassType,

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 50)
    var level: ClassLevel,

    @Column(name = "capacity", nullable = false)
    var capacity: Int,

    @Column(name = "duration_minutes", nullable = false)
    var durationMinutes: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_restriction", length = 20)
    var genderRestriction: Gender? = null,

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,

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
        branchId = UUID.randomUUID(),
        name = "",
        type = ClassType.OTHER,
        level = ClassLevel.ALL_LEVELS,
        capacity = 0,
        durationMinutes = 0
    )
}
