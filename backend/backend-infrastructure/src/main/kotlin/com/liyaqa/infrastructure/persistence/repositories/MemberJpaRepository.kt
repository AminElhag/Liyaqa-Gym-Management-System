package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.infrastructure.persistence.entities.MemberJpaEntity
import com.liyaqa.infrastructure.persistence.mappers.MemberEntityMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository interface for MemberJpaEntity.
 */
interface MemberJpaEntityRepository : JpaRepository<MemberJpaEntity, UUID> {

    @Query("SELECT m FROM MemberJpaEntity m WHERE m.contactInfo.email = :email AND m.isDeleted = false")
    fun findByEmail(@Param("email") email: String): Optional<MemberJpaEntity>

    @Query("SELECT m FROM MemberJpaEntity m WHERE m.branchId = :branchId AND m.isDeleted = false")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<MemberJpaEntity>

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MemberJpaEntity m WHERE m.contactInfo.email = :email AND m.isDeleted = false")
    fun existsByEmail(@Param("email") email: String): Boolean
}

/**
 * Implementation of MemberRepository using Spring Data JPA.
 */
@Repository
class MemberJpaRepositoryImpl(
    private val jpaRepository: MemberJpaEntityRepository,
    private val mapper: MemberEntityMapper
) : MemberRepository {

    override fun findById(id: UUID): Result<Optional<Member>> {
        return runCatching {
            jpaRepository.findById(id)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByEmail(email: String): Result<Optional<Member>> {
        return runCatching {
            jpaRepository.findByEmail(email)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByBranch(branchId: UUID, page: Int, size: Int): Result<List<Member>> {
        return runCatching {
            val pageable = PageRequest.of(page, size)
            jpaRepository.findByBranchId(branchId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun save(member: Member): Result<Member> {
        return runCatching {
            val entity = mapper.toEntity(member)
            val saved = jpaRepository.save(entity)
            mapper.toDomain(saved)
        }
    }

    override fun existsByEmail(email: String): Result<Boolean> {
        return runCatching {
            jpaRepository.existsByEmail(email)
        }
    }
}
