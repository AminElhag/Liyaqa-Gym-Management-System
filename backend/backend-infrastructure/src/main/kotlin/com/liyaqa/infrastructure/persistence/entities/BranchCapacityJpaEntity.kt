package com.liyaqa.infrastructure.persistence.entities

import jakarta.persistence.*
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for BranchCapacity.
 * Tracks real-time occupancy and capacity for each branch.
 */
@Entity
@Table(
    name = "branch_capacity",
    indexes = [
        Index(name = "idx_branch_capacity_branch_id", columnList = "branch_id", unique = true),
        Index(name = "idx_branch_capacity_last_updated", columnList = "last_updated")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class BranchCapacityJpaEntity(
    @Id
    @Column(name = "branch_id", nullable = false, updatable = false)
    val branchId: UUID,

    @Column(name = "max_capacity", nullable = false)
    var maxCapacity: Int,

    @Column(name = "current_occupancy", nullable = false)
    var currentOccupancy: Int = 0,

    @Column(name = "male_count", nullable = false)
    var maleCount: Int = 0,

    @Column(name = "female_count", nullable = false)
    var femaleCount: Int = 0,

    @LastModifiedDate
    @Column(name = "last_updated", nullable = false)
    var lastUpdated: Instant = Instant.now()
) {
    constructor() : this(
        branchId = UUID.randomUUID(),
        maxCapacity = 0
    )
}
