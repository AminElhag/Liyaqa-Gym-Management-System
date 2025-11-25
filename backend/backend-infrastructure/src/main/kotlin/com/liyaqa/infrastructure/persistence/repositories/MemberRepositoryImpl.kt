package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.entities.MemberStatus
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.infrastructure.persistence.mappers.MemberEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Implementation of MemberRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to MemberJpaEntityRepository.
 */
@Repository
class MemberRepositoryImpl(
    private val jpaRepository: MemberJpaEntityRepository,
    private val mapper: MemberEntityMapper
) : MemberRepository {

    private val logger = LoggerFactory.getLogger(MemberRepositoryImpl::class.java)

    override fun findById(id: UUID): Result<Optional<Member>> {
        return runCatching {
            logger.debug("Finding member by ID: {}", id)
            jpaRepository.findById(id)
                .map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find member by ID: {}", id, error)
        }
    }

    override fun findByEmail(email: String): Result<Optional<Member>> {
        return runCatching {
            logger.debug("Finding member by email: {}", email)
            jpaRepository.findByEmail(email)
                .map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find member by email: {}", email, error)
        }
    }

    override fun findByBranch(branchId: UUID, page: Int, size: Int): Result<List<Member>> {
        return runCatching {
            logger.debug("Finding members by branch: {}, page: {}, size: {}", branchId, page, size)
            jpaRepository.findByBranchId(branchId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find members by branch: {}", branchId, error)
        }
    }

    override fun save(member: Member): Result<Member> {
        return runCatching {
            logger.debug("Saving member: {}", member.id)
            val entity = mapper.toEntity(member)
            val saved = jpaRepository.save(entity)
            val savedDomain = mapper.toDomain(saved)
            logger.info("Saved member {} ({})", savedDomain.name, savedDomain.id)
            savedDomain
        }.onFailure { error ->
            logger.error("Failed to save member: {}", member.id, error)
        }
    }

    override fun existsByEmail(email: String): Result<Boolean> {
        return runCatching {
            logger.debug("Checking if member exists by email: {}", email)
            jpaRepository.existsByEmail(email)
        }.onFailure { error ->
            logger.error("Failed to check member existence by email: {}", email, error)
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
            logger.debug("Searching members with criteria - branchId: {}, name: {}, status: {}", branchId, name, status)
            val spec = createSearchSpecification(branchId, name, email, phone, nationalId, status, gender)
            val page = jpaRepository.findAll(spec, pageable)
            logger.debug("Found {} members matching criteria", page.totalElements)
            page.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to search members", error)
        }
    }

    override fun countByBranchAndStatus(branchId: UUID?, status: MemberStatus?): Result<Long> {
        return runCatching {
            logger.debug("Counting members by branch: {} and status: {}", branchId, status)
            when {
                branchId != null && status != null -> jpaRepository.countByBranchIdAndStatus(branchId, status)
                branchId != null -> jpaRepository.countByBranchId(branchId)
                status != null -> jpaRepository.countByStatus(status)
                else -> jpaRepository.count()
            }
        }.onFailure { error ->
            logger.error("Failed to count members", error)
        }
    }

    override fun existsByEmailExcludingMember(email: String, excludeMemberId: UUID): Result<Boolean> {
        return runCatching {
            logger.debug("Checking if email {} exists excluding member: {}", email, excludeMemberId)
            val member = jpaRepository.findById(excludeMemberId)
                .orElseThrow { IllegalArgumentException("Member not found: $excludeMemberId") }
            jpaRepository.existsByEmailAndOrganizationIdAndIdNot(email, member.organizationId, excludeMemberId)
        }.onFailure { error ->
            logger.error("Failed to check email existence excluding member: {}", excludeMemberId, error)
        }
    }

    override fun softDelete(memberId: UUID): Result<Unit> {
        return runCatching {
            logger.debug("Soft deleting member: {}", memberId)
            val entity = jpaRepository.findById(memberId)
                .orElseThrow { IllegalArgumentException("Member not found: $memberId") }

            entity.isDeleted = true
            entity.status = MemberStatus.INACTIVE
            entity.updatedAt = Instant.now()
            jpaRepository.save(entity)
            logger.info("Soft deleted member: {}", memberId)
            Unit
        }.onFailure { error ->
            logger.error("Failed to soft delete member: {}", memberId, error)
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
    ): org.springframework.data.jpa.domain.Specification<com.liyaqa.infrastructure.persistence.entities.MemberJpaEntity> {
        return org.springframework.data.jpa.domain.Specification { root, query, cb ->
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
