package com.liyaqa.gym.database.dao

import com.liyaqa.gym.domain.Invoice
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Invoice operations
 */
interface InvoiceDao {
    /**
     * Get all invoices from cache
     */
    suspend fun getAll(): List<Invoice>

    /**
     * Get invoice by ID from cache
     */
    suspend fun getById(id: String): Invoice?

    /**
     * Get invoices by member ID
     */
    suspend fun getByMemberId(memberId: String): List<Invoice>

    /**
     * Get invoices by subscription ID
     */
    suspend fun getBySubscriptionId(subscriptionId: String): List<Invoice>

    /**
     * Get invoices by status
     */
    suspend fun getByStatus(status: String): List<Invoice>

    /**
     * Get overdue invoices
     */
    suspend fun getOverdue(): List<Invoice>

    /**
     * Observe all invoices as a flow
     */
    fun observeAll(): Flow<List<Invoice>>

    /**
     * Observe invoice by ID as a flow
     */
    fun observeById(id: String): Flow<Invoice?>

    /**
     * Observe invoices by member ID as a flow
     */
    fun observeByMemberId(memberId: String): Flow<List<Invoice>>

    /**
     * Save a single invoice to cache
     */
    suspend fun save(invoice: Invoice)

    /**
     * Save multiple invoices to cache
     */
    suspend fun saveAll(invoices: List<Invoice>)

    /**
     * Update invoice status
     */
    suspend fun updateStatus(id: String, status: String)

    /**
     * Update invoice paid amount
     */
    suspend fun updatePaidAmount(id: String, paidAmount: Double, status: String)

    /**
     * Delete invoice by ID
     */
    suspend fun deleteById(id: String)

    /**
     * Delete invoices by member ID
     */
    suspend fun deleteByMemberId(memberId: String)

    /**
     * Clear all cached invoices
     */
    suspend fun clearAll()

    /**
     * Get count of cached invoices
     */
    suspend fun count(): Long
}
