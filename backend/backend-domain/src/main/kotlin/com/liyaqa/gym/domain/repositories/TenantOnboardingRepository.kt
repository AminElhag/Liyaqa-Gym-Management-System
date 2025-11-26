package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.tenant.TenantOnboardingProgress
import java.util.UUID

/**
 * Repository interface for TenantOnboardingProgress entity operations.
 */
interface TenantOnboardingRepository {

    /**
     * Find onboarding progress by tenant ID and step
     */
    suspend fun findByTenantAndStep(tenantId: UUID, step: String): TenantOnboardingProgress?

    /**
     * Find all onboarding progress for a tenant
     */
    suspend fun findByTenant(tenantId: UUID): List<TenantOnboardingProgress>

    /**
     * Save onboarding progress
     */
    suspend fun save(progress: TenantOnboardingProgress): TenantOnboardingProgress

    /**
     * Save multiple onboarding progress records
     */
    suspend fun saveAll(progressList: List<TenantOnboardingProgress>): List<TenantOnboardingProgress>

    /**
     * Check if tenant has completed all required steps
     */
    suspend fun isOnboardingComplete(tenantId: UUID): Boolean

    /**
     * Delete all onboarding progress for a tenant
     */
    suspend fun deleteByTenant(tenantId: UUID)
}
