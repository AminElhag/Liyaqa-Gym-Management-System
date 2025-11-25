package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.infrastructure.persistence.entities.ZoneJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for ZoneJpaEntity.
 */
@Repository
interface ZoneJpaRepository : JpaRepository<ZoneJpaEntity, UUID> {

    /**
     * Find all zones for a specific branch.
     */
    fun findByBranchId(branchId: UUID): List<ZoneJpaEntity>

    /**
     * Find all active zones for a specific branch.
     */
    @Query("SELECT z FROM ZoneJpaEntity z WHERE z.branchId = :branchId AND z.isActive = true")
    fun findActiveByBranchId(@Param("branchId") branchId: UUID): List<ZoneJpaEntity>
}
