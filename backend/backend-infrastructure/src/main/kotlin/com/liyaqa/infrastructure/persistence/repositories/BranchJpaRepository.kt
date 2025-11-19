package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.infrastructure.persistence.entities.BranchJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for BranchJpaEntity.
 */
@Repository
interface BranchJpaRepository : JpaRepository<BranchJpaEntity, UUID> {

    @Query("SELECT b FROM BranchJpaEntity b WHERE b.organizationId = :organizationId AND b.isDeleted = false")
    fun findByOrganizationId(@Param("organizationId") organizationId: UUID): List<BranchJpaEntity>

    @Query("SELECT b FROM BranchJpaEntity b WHERE b.organizationId = :organizationId AND b.isActive = true AND b.isDeleted = false")
    fun findActiveByOrganizationId(@Param("organizationId") organizationId: UUID): List<BranchJpaEntity>

    @Query("SELECT b FROM BranchJpaEntity b WHERE b.name LIKE %:name% AND b.isDeleted = false")
    fun findByNameContaining(@Param("name") name: String): List<BranchJpaEntity>
}
