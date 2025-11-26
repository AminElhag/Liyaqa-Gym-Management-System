package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.tenant.OnboardingStatus
import com.liyaqa.infrastructure.persistence.entities.TenantOnboardingProgressJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for TenantOnboardingProgress.
 */
@Repository
interface TenantOnboardingJpaRepository : JpaRepository<TenantOnboardingProgressJpaEntity, UUID> {

    /**
     * Find onboarding progress by tenant ID and step
     */
    fun findByTenantIdAndStep(tenantId: UUID, step: String): TenantOnboardingProgressJpaEntity?

    /**
     * Find all onboarding progress for a tenant, ordered by created date
     */
    fun findByTenantIdOrderByCreatedAtAsc(tenantId: UUID): List<TenantOnboardingProgressJpaEntity>

    /**
     * Count completed steps for a tenant
     */
    fun countByTenantIdAndStatus(tenantId: UUID, status: OnboardingStatus): Long

    /**
     * Delete all onboarding progress for a tenant
     */
    fun deleteByTenantId(tenantId: UUID)

    /**
     * Check if all required steps are completed
     */
    @Query(
        """
        SELECT COUNT(p) = 0
        FROM TenantOnboardingProgressJpaEntity p
        WHERE p.tenantId = :tenantId
        AND p.status != 'COMPLETED'
        """
    )
    fun isOnboardingComplete(tenantId: UUID): Boolean
}
