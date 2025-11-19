package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.ClassLevel
import com.liyaqa.gym.domain.entities.ClassType
import com.liyaqa.infrastructure.persistence.entities.ClassJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for ClassJpaEntity.
 */
@Repository
interface ClassJpaRepository : JpaRepository<ClassJpaEntity, UUID> {

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.branchId = :branchId AND c.isDeleted = false")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<ClassJpaEntity>

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.branchId = :branchId AND c.isActive = true AND c.isDeleted = false")
    fun findActiveByBranchId(@Param("branchId") branchId: UUID): List<ClassJpaEntity>

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.type = :type AND c.isActive = true AND c.isDeleted = false")
    fun findByType(@Param("type") type: ClassType): List<ClassJpaEntity>

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.level = :level AND c.isActive = true AND c.isDeleted = false")
    fun findByLevel(@Param("level") level: ClassLevel): List<ClassJpaEntity>

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.name LIKE %:name% AND c.isDeleted = false")
    fun findByNameContaining(@Param("name") name: String): List<ClassJpaEntity>
}
