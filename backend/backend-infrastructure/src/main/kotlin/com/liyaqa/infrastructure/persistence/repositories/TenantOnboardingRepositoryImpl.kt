package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.tenant.OnboardingStatus
import com.liyaqa.gym.domain.entities.tenant.TenantOnboardingProgress
import com.liyaqa.gym.domain.repositories.TenantOnboardingRepository
import com.liyaqa.infrastructure.persistence.mappers.TenantOnboardingMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Implementation of TenantOnboardingRepository using Spring Data JPA.
 */
@Repository
@Transactional
class TenantOnboardingRepositoryImpl(
    private val jpaRepository: TenantOnboardingJpaRepository,
    private val mapper: TenantOnboardingMapper
) : TenantOnboardingRepository {

    private val logger = LoggerFactory.getLogger(TenantOnboardingRepositoryImpl::class.java)

    override suspend fun findByTenantAndStep(tenantId: UUID, step: String): TenantOnboardingProgress? {
        return withContext(Dispatchers.IO) {
            logger.debug("Finding onboarding progress for tenant: $tenantId, step: $step")
            val entity = jpaRepository.findByTenantIdAndStep(tenantId, step)
            entity?.let { mapper.toDomain(it) }
        }
    }

    override suspend fun findByTenant(tenantId: UUID): List<TenantOnboardingProgress> {
        return withContext(Dispatchers.IO) {
            logger.debug("Finding all onboarding progress for tenant: $tenantId")
            val entities = jpaRepository.findByTenantIdOrderByCreatedAtAsc(tenantId)
            entities.map { mapper.toDomain(it) }
        }
    }

    override suspend fun save(progress: TenantOnboardingProgress): TenantOnboardingProgress {
        return withContext(Dispatchers.IO) {
            logger.debug("Saving onboarding progress: ${progress.id}")
            val entity = mapper.toEntity(progress)
            val savedEntity = jpaRepository.save(entity)
            mapper.toDomain(savedEntity)
        }
    }

    override suspend fun saveAll(progressList: List<TenantOnboardingProgress>): List<TenantOnboardingProgress> {
        return withContext(Dispatchers.IO) {
            logger.debug("Saving ${progressList.size} onboarding progress records")
            val entities = progressList.map { mapper.toEntity(it) }
            val savedEntities = jpaRepository.saveAll(entities)
            savedEntities.map { mapper.toDomain(it) }
        }
    }

    override suspend fun isOnboardingComplete(tenantId: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            logger.debug("Checking if onboarding is complete for tenant: $tenantId")
            jpaRepository.isOnboardingComplete(tenantId)
        }
    }

    override suspend fun deleteByTenant(tenantId: UUID) {
        withContext(Dispatchers.IO) {
            logger.debug("Deleting onboarding progress for tenant: $tenantId")
            jpaRepository.deleteByTenantId(tenantId)
        }
    }
}
