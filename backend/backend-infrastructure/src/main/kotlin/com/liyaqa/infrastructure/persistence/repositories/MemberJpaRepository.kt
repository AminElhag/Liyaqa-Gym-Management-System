package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.entities.MemberStatus
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.infrastructure.persistence.entities.MemberJpaEntity
import com.liyaqa.infrastructure.persistence.mappers.MemberEntityMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository interface for MemberJpaEntity.
 */
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

    override fun search(
        branchId: UUID?,
        name: String?,
        email: String?,
        phone: String?,
        nationalId: String?,
        status: MemberStatus?,
        gender: Gender?,
        pageable: Pageable
    ): Result<Page<Member>> {
        return runCatching {
            val spec = createSearchSpecification(branchId, name, email, phone, nationalId, status, gender)
            val page = jpaRepository.findAll(spec, pageable)
            page.map { mapper.toDomain(it) }
        }
    }

    override fun countByBranchAndStatus(branchId: UUID?, status: MemberStatus?): Result<Long> {
        return runCatching {
            when {
                branchId != null && status != null -> jpaRepository.countByBranchIdAndStatus(branchId, status)
                branchId != null -> jpaRepository.countByBranchId(branchId)
                status != null -> jpaRepository.countByStatus(status)
                else -> jpaRepository.count()
            }
        }
    }

    override fun existsByEmailExcludingMember(email: String, excludeMemberId: UUID): Result<Boolean> {
        return runCatching {
            // This needs organizationId - we'll get it from the member being updated
            val member = jpaRepository.findById(excludeMemberId)
                .orElseThrow { IllegalArgumentException("Member not found: $excludeMemberId") }
            jpaRepository.existsByEmailAndOrganizationIdAndIdNot(email, member.organizationId, excludeMemberId)
        }
    }

    override fun softDelete(memberId: UUID): Result<Unit> {
        return runCatching {
            val entity = jpaRepository.findById(memberId)
                .orElseThrow { IllegalArgumentException("Member not found: $memberId") }

            entity.isDeleted = true
            entity.status = MemberStatus.INACTIVE
            entity.updatedAt = Instant.now()
            jpaRepository.save(entity)
            Unit
        }
    }

    private fun createSearchSpecification(
        branchId: UUID?,
        name: String?,
        email: String?,
        phone: String?,
        nationalId: String?,
        status: MemberStatus?,
        gender: Gender?
    ): Specification<MemberJpaEntity> {
        return Specification { root, query, cb ->
            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()

            // Always filter out deleted members
            predicates.add(cb.isFalse(root.get("isDeleted")))

            branchId?.let {
                predicates.add(cb.equal(root.get<UUID>("branchId"), it))
            }

            name?.let {
                predicates.add(
                    cb.or(
                        cb.like(cb.lower(root.get("name")), "%${it.lowercase()}%"),
                        root.get<String?>("nameArabic")?.let { nameArabic ->
                            cb.like(cb.lower(nameArabic), "%${it.lowercase()}%")
                        } ?: cb.conjunction()
                    )
                )
            }

            email?.let {
                predicates.add(
                    cb.like(cb.lower(root.get<Any>("contactInfo").get("email")), "%${it.lowercase()}%")
                )
            }

            phone?.let {
                predicates.add(
                    cb.like(root.get<Any>("contactInfo").get("phone"), "%$it%")
                )
            }

            nationalId?.let {
                predicates.add(cb.equal(root.get<String>("nationalId"), it))
            }

            status?.let {
                predicates.add(cb.equal(root.get<MemberStatus>("status"), it))
            }

            gender?.let {
                predicates.add(cb.equal(root.get<Gender>("gender"), it))
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
