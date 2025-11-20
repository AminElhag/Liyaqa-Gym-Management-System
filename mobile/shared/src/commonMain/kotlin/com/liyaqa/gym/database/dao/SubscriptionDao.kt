package com.liyaqa.gym.database.dao

import com.liyaqa.gym.domain.Subscription
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Subscription operations
 * Implements cache-aside pattern: check cache first, then network
 */
interface SubscriptionDao {
    /**
     * Get all subscriptions from cache
     */
    suspend fun getAll(): List<Subscription>

    /**
     * Get subscription by ID from cache
     */
    suspend fun getById(id: String): Subscription?

    /**
     * Get subscriptions by member ID
     */
    suspend fun getByMemberId(memberId: String): List<Subscription>

    /**
     * Get active subscriptions by member ID
     */
    suspend fun getActiveByMemberId(memberId: String): List<Subscription>

    /**
     * Get subscriptions by status
     */
    suspend fun getByStatus(status: String): List<Subscription>

    /**
     * Get subscriptions expiring soon (within 7 days)
     */
    suspend fun getExpiringSoon(): List<Subscription>

    /**
     * Observe all subscriptions as a flow
     */
    fun observeAll(): Flow<List<Subscription>>

    /**
     * Observe subscription by ID as a flow
     */
    fun observeById(id: String): Flow<Subscription?>

    /**
     * Observe subscriptions by member ID as a flow
     */
    fun observeByMemberId(memberId: String): Flow<List<Subscription>>

    /**
     * Save a single subscription to cache
     */
    suspend fun save(subscription: Subscription)

    /**
     * Save multiple subscriptions to cache
     */
    suspend fun saveAll(subscriptions: List<Subscription>)

    /**
     * Update subscription status
     */
    suspend fun updateStatus(id: String, status: String)

    /**
     * Delete subscription by ID
     */
    suspend fun deleteById(id: String)

    /**
     * Delete subscriptions by member ID
     */
    suspend fun deleteByMemberId(memberId: String)

    /**
     * Clear all cached subscriptions
     */
    suspend fun clearAll()

    /**
     * Get count of cached subscriptions
     */
    suspend fun count(): Long
}
