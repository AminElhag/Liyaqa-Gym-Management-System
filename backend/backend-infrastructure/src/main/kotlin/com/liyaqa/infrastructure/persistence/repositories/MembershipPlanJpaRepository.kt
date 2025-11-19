package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.PlanType
import com.liyaqa.infrastructure.persistence.entities.MembershipPlanJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for MembershipPlanJpaEntity.
 */
@Repository
interface MembershipPlanJpaRepository : JpaRepository<MembershipPlanJpaEntity, UUID> {

    @Query("SELECT m FROM MembershipPlanJpaEntity m WHERE m.branchId = :branchId AND m.isDeleted = false")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<MembershipPlanJpaEntity>

    @Query("SELECT m FROM MembershipPlanJpaEntity m WHERE m.branchId = :branchId AND m.isActive = true AND m.isDeleted = false")
    fun findActiveByBranchId(@Param("branchId") branchId: UUID): List<MembershipPlanJpaEntity>

    @Query("SELECT m FROM MembershipPlanJpaEntity m WHERE m.type = :type AND m.isActive = true AND m.isDeleted = false")
    fun findByType(@Param("type") type: PlanType): List<MembershipPlanJpaEntity>

    @Query("SELECT m FROM MembershipPlanJpaEntity m WHERE m.name LIKE %:name% AND m.isDeleted = false")
    fun findByNameContaining(@Param("name") name: String): List<MembershipPlanJpaEntity>
}
