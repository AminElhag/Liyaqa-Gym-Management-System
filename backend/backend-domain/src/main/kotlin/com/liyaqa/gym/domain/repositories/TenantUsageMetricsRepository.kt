package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.tenant.TenantUsageMetrics
import java.time.YearMonth
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for TenantUsageMetrics entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface TenantUsageMetricsRepository {

    /**
     * Find metrics by unique identifier.
     *
     * @param id The unique identifier of the metrics
     * @return Optional containing the metrics if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<TenantUsageMetrics>>

    /**
     * Find metrics for a tenant and period.
     *
     * @param tenantId The tenant identifier
     * @param period The period (year-month)
     * @return Optional containing the metrics if found, empty otherwise
     */
    fun findByTenantAndPeriod(tenantId: UUID, period: YearMonth): Result<Optional<TenantUsageMetrics>>

    /**
     * Find all metrics for a tenant.
     *
     * @param tenantId The tenant identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return List of metrics ordered by period descending
     */
    fun findByTenant(tenantId: UUID, page: Int = 0, size: Int = 12): Result<List<TenantUsageMetrics>>

    /**
     * Find metrics within a period range for a tenant.
     *
     * @param tenantId The tenant identifier
     * @param startPeriod The start period (inclusive)
     * @param endPeriod The end period (inclusive)
     * @return List of metrics ordered by period ascending
     */
    fun findByTenantAndPeriodBetween(
        tenantId: UUID,
        startPeriod: YearMonth,
        endPeriod: YearMonth
    ): Result<List<TenantUsageMetrics>>

    /**
     * Save metrics (create or update).
     *
     * @param metrics The metrics to save
     * @return The saved metrics
     */
    fun save(metrics: TenantUsageMetrics): Result<TenantUsageMetrics>

    /**
     * Delete metrics by tenant and period.
     *
     * @param tenantId The tenant identifier
     * @param period The period to delete
     * @return Result indicating success
     */
    fun deleteByTenantAndPeriod(tenantId: UUID, period: YearMonth): Result<Unit>
}
