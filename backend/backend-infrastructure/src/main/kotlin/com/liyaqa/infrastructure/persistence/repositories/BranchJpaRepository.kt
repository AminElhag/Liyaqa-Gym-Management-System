package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Branch
import com.liyaqa.gym.domain.repositories.BranchRepository
import com.liyaqa.infrastructure.persistence.entities.BranchJpaEntity
import com.liyaqa.infrastructure.persistence.mappers.BranchEntityMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository interface for BranchJpaEntity.
 */
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
}

/**
 * Implementation of BranchRepository using Spring Data JPA.
 */
@Repository
class BranchJpaRepositoryImpl(
    private val jpaRepository: BranchJpaEntityRepository,
    private val mapper: BranchEntityMapper
) : BranchRepository {

    override fun findById(id: UUID): Result<Optional<Branch>> {
        return runCatching {
            jpaRepository.findById(id)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByOrganization(organizationId: UUID, page: Int, size: Int): Result<List<Branch>> {
        return runCatching {
            jpaRepository.findByOrganizationId(organizationId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findActive(page: Int, size: Int): Result<List<Branch>> {
        return runCatching {
            jpaRepository.findAllActive()
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByName(name: String, page: Int, size: Int): Result<List<Branch>> {
        return runCatching {
            jpaRepository.findByNameContaining(name)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun save(branch: Branch): Result<Branch> {
        return runCatching {
            val entity = mapper.toEntity(branch)
            val saved = jpaRepository.save(entity)
            mapper.toDomain(saved)
        }
    }

    override fun existsByOrganizationAndName(organizationId: UUID, name: String): Result<Boolean> {
        return runCatching {
            jpaRepository.existsByOrganizationIdAndName(organizationId, name)
        }
    }
}
