package com.liyaqa.gym.database.dao

import com.liyaqa.gym.domain.Payment
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Payment operations
 */
interface PaymentDao {
    /**
     * Get all payments from cache
     */
    suspend fun getAll(): List<Payment>

    /**
     * Get payment by ID from cache
     */
    suspend fun getById(id: String): Payment?

    /**
     * Get payments by member ID
     */
    suspend fun getByMemberId(memberId: String): List<Payment>

    /**
     * Get payments by subscription ID
     */
    suspend fun getBySubscriptionId(subscriptionId: String): List<Payment>

    /**
     * Get payments by status
     */
    suspend fun getByStatus(status: String): List<Payment>

    /**
     * Get recent payments (last 30 days)
     */
    suspend fun getRecent(): List<Payment>

    /**
     * Observe all payments as a flow
     */
    fun observeAll(): Flow<List<Payment>>

    /**
     * Observe payment by ID as a flow
     */
    fun observeById(id: String): Flow<Payment?>

    /**
     * Observe payments by member ID as a flow
     */
    fun observeByMemberId(memberId: String): Flow<List<Payment>>

    /**
     * Save a single payment to cache
     */
    suspend fun save(payment: Payment)

    /**
     * Save multiple payments to cache
     */
    suspend fun saveAll(payments: List<Payment>)

    /**
     * Update payment status
     */
    suspend fun updateStatus(id: String, status: String)

    /**
     * Delete payment by ID
     */
    suspend fun deleteById(id: String)

    /**
     * Delete payments by member ID
     */
    suspend fun deleteByMemberId(memberId: String)

    /**
     * Clear all cached payments
     */
    suspend fun clearAll()

    /**
     * Get count of cached payments
     */
    suspend fun count(): Long
}
