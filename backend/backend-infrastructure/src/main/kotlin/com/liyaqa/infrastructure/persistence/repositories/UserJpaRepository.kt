package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.User
import com.liyaqa.gym.domain.repositories.UserRepository
import com.liyaqa.infrastructure.persistence.entities.UserJpaEntity
import com.liyaqa.infrastructure.persistence.mappers.UserEntityMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository interface for UserJpaEntity.
 */
interface UserJpaEntityRepository : JpaRepository<UserJpaEntity, UUID> {

    @Query("SELECT u FROM UserJpaEntity u WHERE u.email = :email AND u.isActive = true")
    fun findByEmailAndActive(@Param("email") email: String): UserJpaEntity?

    @Query("SELECT u FROM UserJpaEntity u WHERE u.email = :email AND u.organizationId = :organizationId AND u.isActive = true")
    fun findByEmailAndOrganizationId(
        @Param("email") email: String,
        @Param("organizationId") organizationId: UUID
    ): UserJpaEntity?

    @Query("SELECT u FROM UserJpaEntity u WHERE u.organizationId = :organizationId AND u.isActive = true")
    fun findAllByOrganizationId(@Param("organizationId") organizationId: UUID): List<UserJpaEntity>

    @Query("SELECT u FROM UserJpaEntity u WHERE u.branchId = :branchId AND u.isActive = true")
    fun findAllByBranchId(@Param("branchId") branchId: UUID): List<UserJpaEntity>

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserJpaEntity u WHERE u.email = :email AND u.isActive = true")
    fun existsByEmailAndActive(@Param("email") email: String): Boolean

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserJpaEntity u WHERE u.email = :email AND u.organizationId = :organizationId AND u.isActive = true")
    fun existsByEmailAndOrganizationId(
        @Param("email") email: String,
        @Param("organizationId") organizationId: UUID
    ): Boolean
}

/**
 * Implementation of UserRepository using Spring Data JPA.
 */
@Repository
class UserJpaRepositoryImpl(
    private val jpaRepository: UserJpaEntityRepository,
    private val mapper: UserEntityMapper
) : UserRepository {

    override fun findById(id: UUID): User? {
        return jpaRepository.findById(id)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByEmail(email: String): User? {
        return jpaRepository.findByEmailAndActive(email)
            ?.let { mapper.toDomain(it) }
    }

    override fun findByEmailAndOrganizationId(email: String, organizationId: UUID): User? {
        return jpaRepository.findByEmailAndOrganizationId(email, organizationId)
            ?.let { mapper.toDomain(it) }
    }

    override fun save(user: User): User {
        val entity = mapper.toEntity(user)
        val saved = jpaRepository.save(entity)
        return mapper.toDomain(saved)
    }

    override fun delete(id: UUID) {
        // Soft delete by marking as inactive
        jpaRepository.findById(id).ifPresent { user ->
            user.isActive = false
            jpaRepository.save(user)
        }
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmailAndActive(email)
    }

    override fun existsByEmailAndOrganizationId(email: String, organizationId: UUID): Boolean {
        return jpaRepository.existsByEmailAndOrganizationId(email, organizationId)
    }

    override fun findAllByOrganizationId(organizationId: UUID): List<User> {
        return jpaRepository.findAllByOrganizationId(organizationId)
            .map { mapper.toDomain(it) }
    }

    override fun findAllByBranchId(branchId: UUID): List<User> {
        return jpaRepository.findAllByBranchId(branchId)
            .map { mapper.toDomain(it) }
    }
}