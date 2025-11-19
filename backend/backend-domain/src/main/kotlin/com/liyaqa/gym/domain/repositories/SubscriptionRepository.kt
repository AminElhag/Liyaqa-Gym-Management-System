package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Subscription
import com.liyaqa.gym.domain.entities.SubscriptionStatus
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Subscription entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface SubscriptionRepository {

    /**
     * Find a subscription by its unique identifier.
     *
     * @param id The unique identifier of the subscription
     * @return Optional containing the subscription if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Subscription>>

    /**
     * Find all subscriptions for a specific member.
     *
     * @param memberId The member identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of subscriptions
     */
    fun findByMember(memberId: UUID, page: Int = 0, size: Int = 20): Result<List<Subscription>>

    /**
     * Find subscriptions by status.
     *
     * @param status The subscription status to filter by
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of subscriptions
     */
    fun findByStatus(status: SubscriptionStatus, page: Int = 0, size: Int = 20): Result<List<Subscription>>

    /**
     * Find subscriptions expiring within a date range.
     *
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of subscriptions expiring in the range
     */
    fun findExpiringBetween(
        startDate: LocalDate,
        endDate: LocalDate,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Subscription>>

    /**
     * Save a subscription (create or update).
     *
     * @param subscription The subscription to save
     * @return Result containing the saved subscription
     */
    fun save(subscription: Subscription): Result<Subscription>
}
