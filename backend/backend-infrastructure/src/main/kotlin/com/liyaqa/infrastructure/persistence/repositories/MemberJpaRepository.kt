package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.MemberStatus
import com.liyaqa.infrastructure.persistence.entities.MemberJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository interface for MemberJpaEntity.
 */
@Repository
interface MemberJpaEntityRepository : JpaRepository<MemberJpaEntity, UUID>, JpaSpecificationExecutor<MemberJpaEntity> {

    @Query("SELECT m FROM MemberJpaEntity m WHERE m.contactInfo.email = :email AND m.isDeleted = false")
    fun findByEmail(@Param("email") email: String): Optional<MemberJpaEntity>

    @Query("SELECT m FROM MemberJpaEntity m WHERE m.branchId = :branchId AND m.isDeleted = false")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<MemberJpaEntity>

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MemberJpaEntity m WHERE m.contactInfo.email = :email AND m.isDeleted = false")
    fun existsByEmail(@Param("email") email: String): Boolean

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MemberJpaEntity m WHERE m.contactInfo.email = :email AND m.organizationId = :organizationId AND m.id != :excludeId AND m.isDeleted = false")
    fun existsByEmailAndOrganizationIdAndIdNot(
        @Param("email") email: String,
        @Param("organizationId") organizationId: UUID,
        @Param("excludeId") excludeId: UUID
    ): Boolean

    @Query("SELECT COUNT(m) FROM MemberJpaEntity m WHERE m.branchId = :branchId AND m.isDeleted = false")
    fun countByBranchId(@Param("branchId") branchId: UUID): Long

    @Query("SELECT COUNT(m) FROM MemberJpaEntity m WHERE m.status = :status AND m.isDeleted = false")
    fun countByStatus(@Param("status") status: MemberStatus): Long

    @Query("SELECT COUNT(m) FROM MemberJpaEntity m WHERE m.branchId = :branchId AND m.status = :status AND m.isDeleted = false")
    fun countByBranchIdAndStatus(@Param("branchId") branchId: UUID, @Param("status") status: MemberStatus): Long
}
