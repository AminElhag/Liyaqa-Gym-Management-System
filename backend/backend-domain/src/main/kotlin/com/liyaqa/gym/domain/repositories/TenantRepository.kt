package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.tenant.Tenant
import com.liyaqa.gym.domain.entities.tenant.TenantStatus
import com.liyaqa.gym.domain.entities.tenant.TenantSubscriptionPlan
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Tenant entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface TenantRepository {

    /**
     * Find a tenant by its unique identifier.
     *
     * @param id The unique identifier of the tenant
     * @return Optional containing the tenant if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Tenant>>

    /**
     * Find a tenant by its slug.
     *
     * @param slug The tenant slug (subdomain identifier)
     * @return Optional containing the tenant if found, empty otherwise
     */
    fun findBySlug(slug: String): Result<Optional<Tenant>>

    /**
     * Find all tenants with pagination.
     *
     * @param pageable Pagination and sorting parameters
     * @return Page of tenants
     */
    fun findAll(pageable: Pageable): Result<Page<Tenant>>

    /**
     * Find tenants by status.
     *
     * @param status The tenant status to filter by
     * @param pageable Pagination and sorting parameters
     * @return Page of tenants
     */
    fun findByStatus(status: TenantStatus, pageable: Pageable): Result<Page<Tenant>>

    /**
     * Find tenants by subscription plan.
     *
     * @param plan The subscription plan to filter by
     * @param pageable Pagination and sorting parameters
     * @return Page of tenants
     */
    fun findByPlan(plan: TenantSubscriptionPlan, pageable: Pageable): Result<Page<Tenant>>

    /**
     * Search tenants with dynamic criteria.
     *
     * @param name Search by name (partial match, case-insensitive)
     * @param slug Search by slug (partial match)
     * @param status Filter by status (optional)
     * @param plan Filter by subscription plan (optional)
     * @param pageable Pagination and sorting parameters
     * @return Page of tenants
     */
    fun search(
        name: String?,
        slug: String?,
        status: TenantStatus?,
        plan: TenantSubscriptionPlan?,
        pageable: Pageable
    ): Result<Page<Tenant>>

    /**
     * Save a tenant (create or update).
     *
     * @param tenant The tenant to save
     * @return The saved tenant
     */
    fun save(tenant: Tenant): Result<Tenant>

    /**
     * Check if a tenant with the given slug exists.
     *
     * @param slug The slug to check
     * @return true if exists, false otherwise
     */
    fun existsBySlug(slug: String): Result<Boolean>

    /**
     * Check if a tenant with the given slug exists (excluding a specific tenant ID).
     *
     * @param slug The slug to check
     * @param excludeTenantId The tenant ID to exclude from the check
     * @return true if exists, false otherwise
     */
    fun existsBySlugExcludingTenant(slug: String, excludeTenantId: UUID): Result<Boolean>

    /**
     * Count tenants by status.
     *
     * @param status The tenant status to filter by
     * @return Count of tenants
     */
    fun countByStatus(status: TenantStatus): Result<Long>

    /**
     * Count total tenants.
     *
     * @return Count of all tenants
     */
    fun count(): Result<Long>

    /**
     * Soft delete a tenant.
     *
     * @param tenantId The tenant ID to delete
     * @return Result indicating success
     */
    fun softDelete(tenantId: UUID): Result<Unit>

    /**
     * Find a tenant by its custom domain.
     *
     * @param domain The custom domain to search for
     * @return Optional containing the tenant if found, empty otherwise
     */
    fun findByCustomDomain(domain: String): Result<Optional<Tenant>>

    /**
     * Check if a custom domain is already in use.
     *
     * @param domain The custom domain to check
     * @return true if exists, false otherwise
     */
    fun existsByCustomDomain(domain: String): Result<Boolean>

    /**
     * Check if a custom domain is already in use (excluding a specific tenant).
     *
     * @param domain The custom domain to check
     * @param excludeTenantId The tenant ID to exclude from the check
     * @return true if exists, false otherwise
     */
    fun existsByCustomDomainExcludingTenant(domain: String, excludeTenantId: UUID): Result<Boolean>
}
