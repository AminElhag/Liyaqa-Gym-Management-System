package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.tenant.SubscriptionStatus
import com.liyaqa.gym.domain.entities.tenant.TenantSubscription
import com.liyaqa.gym.domain.entities.tenant.TenantSubscriptionPlan
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for TenantSubscription entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface TenantSubscriptionRepository {

    /**
     * Find a subscription by its unique identifier.
     *
     * @param id The unique identifier of the subscription
     * @return Optional containing the subscription if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<TenantSubscription>>

    /**
     * Find the active subscription for a tenant.
     *
     * @param tenantId The tenant identifier
     * @return Optional containing the active subscription if found
     */
    fun findActiveByTenant(tenantId: UUID): Result<Optional<TenantSubscription>>

    /**
     * Find all subscriptions for a tenant.
     *
     * @param tenantId The tenant identifier
     * @param pageable Pagination and sorting parameters
     * @return Page of subscriptions
     */
    fun findByTenant(tenantId: UUID, pageable: Pageable): Result<Page<TenantSubscription>>

    /**
     * Find subscriptions by status.
     *
     * @param status The subscription status
     * @param pageable Pagination and sorting parameters
     * @return Page of subscriptions
     */
    fun findByStatus(status: SubscriptionStatus, pageable: Pageable): Result<Page<TenantSubscription>>

    /**
     * Find subscriptions expiring within a date range.
     *
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @param pageable Pagination and sorting parameters
     * @return Page of subscriptions
     */
    fun findExpiringBetween(
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ): Result<Page<TenantSubscription>>

    /**
     * Find subscriptions with next billing date in range.
     *
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @param pageable Pagination and sorting parameters
     * @return Page of subscriptions
     */
    fun findWithBillingDueBetween(
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ): Result<Page<TenantSubscription>>

    /**
     * Save a subscription (create or update).
     *
     * @param subscription The subscription to save
     * @return The saved subscription
     */
    fun save(subscription: TenantSubscription): Result<TenantSubscription>

    /**
     * Count subscriptions by status.
     *
     * @param status The subscription status
     * @return Count of subscriptions
     */
    fun countByStatus(status: SubscriptionStatus): Result<Long>
}
