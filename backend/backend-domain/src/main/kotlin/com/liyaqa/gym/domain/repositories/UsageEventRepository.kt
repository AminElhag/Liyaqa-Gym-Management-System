package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.tenant.UsageEvent
import com.liyaqa.gym.domain.entities.tenant.UsageEventType
import java.time.Instant
import java.time.YearMonth
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for UsageEvent entity operations.
 * Manages persistence of usage events for billing calculations.
 */
interface UsageEventRepository {

    /**
     * Find usage event by unique identifier.
     *
     * @param id The unique identifier of the usage event
     * @return Optional containing the event if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<UsageEvent>>

    /**
     * Find all usage events for a tenant within a specific period.
     *
     * @param tenantId The tenant identifier
     * @param period The billing period (year-month)
     * @return List of usage events for the period
     */
    fun findByTenantAndPeriod(tenantId: UUID, period: YearMonth): Result<List<UsageEvent>>

    /**
     * Find usage events by tenant, period, and event type.
     *
     * @param tenantId The tenant identifier
     * @param period The billing period (year-month)
     * @param eventType The type of usage event
     * @return List of usage events matching the criteria
     */
    fun findByTenantPeriodAndType(
        tenantId: UUID,
        period: YearMonth,
        eventType: UsageEventType
    ): Result<List<UsageEvent>>

    /**
     * Find usage events for a tenant within a date range.
     *
     * @param tenantId The tenant identifier
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of usage events ordered by occurrence time
     */
    fun findByTenantAndDateRange(
        tenantId: UUID,
        startDate: Instant,
        endDate: Instant
    ): Result<List<UsageEvent>>

    /**
     * Count usage events for a tenant and period by type.
     *
     * @param tenantId The tenant identifier
     * @param period The billing period
     * @param eventType The type of usage event
     * @return Count of events
     */
    fun countByTenantPeriodAndType(
        tenantId: UUID,
        period: YearMonth,
        eventType: UsageEventType
    ): Result<Long>

    /**
     * Sum quantity of usage events for a tenant and period by type.
     *
     * @param tenantId The tenant identifier
     * @param period The billing period
     * @param eventType The type of usage event
     * @return Sum of event quantities
     */
    fun sumQuantityByTenantPeriodAndType(
        tenantId: UUID,
        period: YearMonth,
        eventType: UsageEventType
    ): Result<Long>

    /**
     * Save a usage event (create or update).
     *
     * @param event The usage event to save
     * @return The saved usage event
     */
    fun save(event: UsageEvent): Result<UsageEvent>

    /**
     * Save multiple usage events in batch.
     *
     * @param events The list of usage events to save
     * @return List of saved usage events
     */
    fun saveAll(events: List<UsageEvent>): Result<List<UsageEvent>>

    /**
     * Delete usage events older than a specific date.
     * Useful for data retention policies.
     *
     * @param beforeDate Delete events before this date
     * @return Number of deleted events
     */
    fun deleteOlderThan(beforeDate: Instant): Result<Long>
}
