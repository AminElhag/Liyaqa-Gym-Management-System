package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Branch
import com.liyaqa.gym.domain.repositories.BranchRepository
import com.liyaqa.infrastructure.persistence.mappers.BranchEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Implementation of BranchRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to BranchJpaEntityRepository.
 */
@Repository
class BranchRepositoryImpl(
    private val jpaRepository: BranchJpaEntityRepository,
    private val mapper: BranchEntityMapper
) : BranchRepository {

    private val logger = LoggerFactory.getLogger(BranchRepositoryImpl::class.java)

    override fun findById(id: UUID): Result<Optional<Branch>> {
        return runCatching {
            logger.debug("Finding branch by ID: {}", id)
            jpaRepository.findById(id)
                .map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find branch by ID: {}", id, error)
        }
    }

    override fun findByOrganization(organizationId: UUID, page: Int, size: Int): Result<List<Branch>> {
        return runCatching {
            logger.debug("Finding branches by organization: {}, page: {}, size: {}", organizationId, page, size)
            jpaRepository.findByOrganizationId(organizationId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find branches by organization: {}", organizationId, error)
        }
    }

    override fun findActive(page: Int, size: Int): Result<List<Branch>> {
        return runCatching {
            logger.debug("Finding active branches, page: {}, size: {}", page, size)
            jpaRepository.findAllActive()
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find active branches", error)
        }
    }

    override fun findByName(name: String, page: Int, size: Int): Result<List<Branch>> {
        return runCatching {
            logger.debug("Finding branches by name: {}, page: {}, size: {}", name, page, size)
            jpaRepository.findByNameContaining(name)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find branches by name: {}", name, error)
        }
    }

    override fun save(branch: Branch): Result<Branch> {
        return runCatching {
            logger.debug("Saving branch: {}", branch.id)
            val entity = mapper.toEntity(branch)
            val saved = jpaRepository.save(entity)
            val savedDomain = mapper.toDomain(saved)
            logger.info("Saved branch {} ({})", savedDomain.name, savedDomain.id)
            savedDomain
        }.onFailure { error ->
            logger.error("Failed to save branch: {}", branch.id, error)
        }
    }

    override fun existsByOrganizationAndName(organizationId: UUID, name: String): Result<Boolean> {
        return runCatching {
            logger.debug("Checking if branch exists by organization: {} and name: {}", organizationId, name)
            jpaRepository.existsByOrganizationIdAndName(organizationId, name)
        }.onFailure { error ->
            logger.error("Failed to check branch existence by organization and name", error)
        }
    }

    override fun countByOrganization(organizationId: UUID): Result<Long> {
        return runCatching {
            logger.debug("Counting branches by organization: {}", organizationId)
            jpaRepository.countByOrganizationId(organizationId)
        }.onFailure { error ->
            logger.error("Failed to count branches by organization: {}", organizationId, error)
        }
    }
}
