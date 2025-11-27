package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.infrastructure.persistence.entities.BranchJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository interface for BranchJpaEntity.
 */
@Repository
interface BranchJpaEntityRepository : JpaRepository<BranchJpaEntity, UUID> {

    @Query("SELECT b FROM BranchJpaEntity b WHERE b.organizationId = :organizationId AND b.isDeleted = false")
    fun findByOrganizationId(@Param("organizationId") organizationId: UUID): List<BranchJpaEntity>

    @Query("SELECT b FROM BranchJpaEntity b WHERE b.organizationId = :organizationId AND b.isActive = true AND b.isDeleted = false")
    fun findActiveByOrganizationId(@Param("organizationId") organizationId: UUID): List<BranchJpaEntity>

    @Query("SELECT b FROM BranchJpaEntity b WHERE b.name LIKE %:name% AND b.isDeleted = false")
    fun findByNameContaining(@Param("name") name: String): List<BranchJpaEntity>

    @Query("SELECT b FROM BranchJpaEntity b WHERE b.isActive = true AND b.isDeleted = false")
    fun findAllActive(): List<BranchJpaEntity>

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BranchJpaEntity b WHERE b.organizationId = :organizationId AND b.name = :name AND b.isDeleted = false")
    fun existsByOrganizationIdAndName(@Param("organizationId") organizationId: UUID, @Param("name") name: String): Boolean

    @Query("SELECT COUNT(b) FROM BranchJpaEntity b WHERE b.organizationId = :organizationId AND b.isDeleted = false")
    fun countByOrganizationId(@Param("organizationId") organizationId: UUID): Long
}
