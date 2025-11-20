package com.liyaqa.gym.data.repositories

import com.liyaqa.gym.database.dao.InvoiceDao
import com.liyaqa.gym.database.dao.PaymentDao
import com.liyaqa.gym.domain.Invoice
import com.liyaqa.gym.domain.Payment
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.ConnectivityMonitor
import com.liyaqa.gym.network.mappers.toDomain
import com.liyaqa.gym.network.models.PaymentMethod
import com.liyaqa.gym.network.models.PaymentRequest
import com.liyaqa.gym.network.services.PaymentApiService
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.hours

/**
 * Repository interface for Payment operations
 */
interface PaymentRepository {
    /**
     * Process a payment
     * @param subscriptionId The subscription ID
     * @param amount The payment amount
     * @param paymentMethod The payment method
     * @param description Optional payment description
     * @return Result containing Payment or error
     */
    suspend fun processPayment(
        subscriptionId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        description: String? = null
    ): Result<Payment>

    /**
     * Get payment history for a member
     * @param memberId The member ID
     * @param forceRefresh Force fetching from network
     * @return Result containing list of Payment or error
     */
    suspend fun getPaymentHistory(memberId: String, forceRefresh: Boolean = false): Result<List<Payment>>

    /**
     * Get invoices for a member
     * @param memberId The member ID
     * @param forceRefresh Force fetching from network
     * @return Result containing list of Invoice or error
     */
    suspend fun getInvoices(memberId: String, forceRefresh: Boolean = false): Result<List<Invoice>>

    /**
     * Get invoice by ID
     * @param invoiceId The invoice ID
     * @return Result containing Invoice or error
     */
    suspend fun getInvoiceById(invoiceId: String): Result<Invoice>

    /**
     * Pay an invoice
     * @param invoiceId The invoice ID
     * @param amount The payment amount
     * @param paymentMethod The payment method
     * @return Result containing Payment or error
     */
    suspend fun payInvoice(
        invoiceId: String,
        amount: Double,
        paymentMethod: PaymentMethod
    ): Result<Payment>

    /**
     * Observe payment history as a flow
     * @param memberId The member ID
     * @return Flow of list of Payment
     */
    fun observePaymentHistory(memberId: String): Flow<List<Payment>>

    /**
     * Observe invoices as a flow
     * @param memberId The member ID
     * @return Flow of list of Invoice
     */
    fun observeInvoices(memberId: String): Flow<List<Invoice>>

    /**
     * Clear all cached payment and invoice data
     */
    suspend fun clearCache()
}

/**
 * Implementation of PaymentRepository with offline-first capabilities
 */
class PaymentRepositoryImpl(
    private val apiService: PaymentApiService,
    private val paymentDao: PaymentDao,
    private val invoiceDao: InvoiceDao,
    private val connectivityMonitor: ConnectivityMonitor
) : PaymentRepository {

    private val cacheMaxAge = 1.hours

    override suspend fun processPayment(
        subscriptionId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        description: String?
    ): Result<Payment> {
        // Check connectivity - payment processing requires online access
        if (!connectivityMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection. Payment processing requires online access."))
        }

        val request = PaymentRequest(
            subscriptionId = subscriptionId,
            amount = amount,
            paymentMethod = paymentMethod,
            description = description
        )

        // Process payment
        return when (val result = apiService.processPayment(request)) {
            is ApiResult.Success -> {
                val payment = result.data.toDomain()
                // Save to cache
                paymentDao.save(payment)
                Result.success(payment)
            }
            is ApiResult.Error -> {
                Result.failure(result.error)
            }
        }
    }

    override suspend fun getPaymentHistory(memberId: String, forceRefresh: Boolean): Result<List<Payment>> {
        // Try to get cached data first (offline-first approach)
        if (!forceRefresh) {
            val cached = paymentDao.getByMemberId(memberId)
            if (cached.isNotEmpty()) {
                return Result.success(cached)
            }
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            // Offline: return cached data
            val cached = paymentDao.getByMemberId(memberId)
            return if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No internet connection and no cached data available"))
            }
        }

        // Note: We don't have a direct endpoint for member payments
        // This would typically be fetched when getting invoices
        // For now, return cached data
        val cached = paymentDao.getByMemberId(memberId)
        return Result.success(cached)
    }

    override suspend fun getInvoices(memberId: String, forceRefresh: Boolean): Result<List<Invoice>> {
        // Try to get cached data first (offline-first approach)
        if (!forceRefresh) {
            val cached = invoiceDao.getByMemberId(memberId)
            if (cached.isNotEmpty()) {
                return Result.success(cached)
            }
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            // Offline: return cached data
            val cached = invoiceDao.getByMemberId(memberId)
            return if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No internet connection and no cached data available"))
            }
        }

        // Fetch from network
        return when (val result = apiService.getInvoices(memberId, size = 100)) {
            is ApiResult.Success -> {
                val invoices = result.data.content.map { it.toDomain() }
                // Save to cache
                invoiceDao.saveAll(invoices)
                Result.success(invoices)
            }
            is ApiResult.Error -> {
                // Network error: try to return cached data as fallback
                val cached = invoiceDao.getByMemberId(memberId)
                if (cached.isNotEmpty()) {
                    Result.success(cached)
                } else {
                    Result.failure(result.error)
                }
            }
        }
    }

    override suspend fun getInvoiceById(invoiceId: String): Result<Invoice> {
        // Check cache first
        val cached = invoiceDao.getById(invoiceId)
        if (cached != null) {
            return Result.success(cached)
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection and no cached data available"))
        }

        // Fetch from network
        return when (val result = apiService.getInvoiceById(invoiceId)) {
            is ApiResult.Success -> {
                val invoice = result.data.toDomain()
                // Save to cache
                invoiceDao.save(invoice)
                Result.success(invoice)
            }
            is ApiResult.Error -> {
                Result.failure(result.error)
            }
        }
    }

    override suspend fun payInvoice(
        invoiceId: String,
        amount: Double,
        paymentMethod: PaymentMethod
    ): Result<Payment> {
        // Check connectivity - payment processing requires online access
        if (!connectivityMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection. Payment processing requires online access."))
        }

        val request = PaymentRequest(
            subscriptionId = "", // Not needed for invoice payment
            amount = amount,
            paymentMethod = paymentMethod,
            description = "Invoice payment"
        )

        // Pay invoice
        return when (val result = apiService.payInvoice(invoiceId, request)) {
            is ApiResult.Success -> {
                val payment = result.data.toDomain()
                // Save payment to cache
                paymentDao.save(payment)

                // Update invoice in cache
                // In a real implementation, you'd also update the invoice status

                Result.success(payment)
            }
            is ApiResult.Error -> {
                Result.failure(result.error)
            }
        }
    }

    override fun observePaymentHistory(memberId: String): Flow<List<Payment>> {
        return paymentDao.observeByMemberId(memberId)
    }

    override fun observeInvoices(memberId: String): Flow<List<Invoice>> {
        return invoiceDao.observeByMemberId(memberId)
    }

    override suspend fun clearCache() {
        paymentDao.clearAll()
        invoiceDao.clearAll()
    }
}
