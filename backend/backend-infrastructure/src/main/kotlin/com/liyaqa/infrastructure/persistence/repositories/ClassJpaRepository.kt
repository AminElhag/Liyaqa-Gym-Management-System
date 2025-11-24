package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Class
import com.liyaqa.gym.domain.entities.ClassLevel
import com.liyaqa.gym.domain.entities.ClassType
import com.liyaqa.gym.domain.repositories.ClassRepository
import com.liyaqa.infrastructure.persistence.entities.ClassJpaEntity
import com.liyaqa.infrastructure.persistence.mappers.ClassEntityMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository interface for ClassJpaEntity.
 */
interface ClassJpaEntityRepository : JpaRepository<ClassJpaEntity, UUID> {

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.branchId = :branchId AND c.isDeleted = false")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<ClassJpaEntity>

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.branchId = :branchId AND c.isActive = true AND c.isDeleted = false")
    fun findActiveByBranchId(@Param("branchId") branchId: UUID): List<ClassJpaEntity>

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.isActive = true AND c.isDeleted = false")
    fun findAllActive(): List<ClassJpaEntity>

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.type = :type AND c.isActive = true AND c.isDeleted = false")
    fun findByType(@Param("type") type: ClassType): List<ClassJpaEntity>

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.level = :level AND c.isActive = true AND c.isDeleted = false")
    fun findByLevel(@Param("level") level: ClassLevel): List<ClassJpaEntity>

    @Query("SELECT c FROM ClassJpaEntity c WHERE c.name LIKE %:name% AND c.isDeleted = false")
    fun findByNameContaining(@Param("name") name: String): List<ClassJpaEntity>
}

/**
 * Implementation of ClassRepository using Spring Data JPA.
 */
@Repository
class ClassJpaRepositoryImpl(
    private val jpaRepository: ClassJpaEntityRepository,
    private val mapper: ClassEntityMapper
) : ClassRepository {

    override fun findById(id: UUID): Result<Optional<Class>> {
        return runCatching {
            jpaRepository.findById(id)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByBranch(branchId: UUID, page: Int, size: Int): Result<List<Class>> {
        return runCatching {
            jpaRepository.findByBranchId(branchId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findAllActive(page: Int, size: Int): Result<List<Class>> {
        return runCatching {
            jpaRepository.findAllActive()
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun save(gymClass: Class): Result<Class> {
        return runCatching {
            val entity = mapper.toEntity(gymClass)
            val saved = jpaRepository.save(entity)
            mapper.toDomain(saved)
        }
    }
}
