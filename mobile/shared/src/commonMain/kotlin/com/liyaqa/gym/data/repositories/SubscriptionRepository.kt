package com.liyaqa.gym.data.repositories

import com.liyaqa.gym.database.dao.SubscriptionDao
import com.liyaqa.gym.domain.Subscription
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.ConnectivityMonitor
import com.liyaqa.gym.network.mappers.toDomain
import com.liyaqa.gym.network.models.RenewSubscriptionRequest
import com.liyaqa.gym.network.services.SubscriptionApiService
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.hours

/**
 * Repository interface for Subscription operations
 */
interface SubscriptionRepository {
    /**
     * Get member's subscriptions
     * @param memberId The member ID
     * @param forceRefresh Force fetching from network
     * @return Result containing list of Subscription or error
     */
    suspend fun getSubscriptions(memberId: String, forceRefresh: Boolean = false): Result<List<Subscription>>

    /**
     * Get subscription by ID
     * @param subscriptionId The subscription ID
     * @return Result containing Subscription or error
     */
    suspend fun getSubscriptionById(subscriptionId: String): Result<Subscription>

    /**
     * Renew a subscription
     * @param subscriptionId The subscription ID to renew
     * @param planId The new plan ID
     * @param startDate The start date for the renewal
     * @param autoRenew Whether to auto-renew
     * @return Result containing renewed Subscription or error
     */
    suspend fun renewSubscription(
        subscriptionId: String,
        planId: String,
        startDate: String,
        autoRenew: Boolean = false
    ): Result<Subscription>

    /**
     * Get active subscriptions for a member
     * @param memberId The member ID
     * @return Result containing list of active Subscription or error
     */
    suspend fun getActiveSubscriptions(memberId: String): Result<List<Subscription>>

    /**
     * Observe member's subscriptions as a flow
     * @param memberId The member ID
     * @return Flow of list of Subscription
     */
    fun observeSubscriptions(memberId: String): Flow<List<Subscription>>

    /**
     * Clear all cached subscription data
     */
    suspend fun clearCache()
}

/**
 * Implementation of SubscriptionRepository with offline-first capabilities
 */
class SubscriptionRepositoryImpl(
    private val apiService: SubscriptionApiService,
    private val subscriptionDao: SubscriptionDao,
    private val connectivityMonitor: ConnectivityMonitor
) : SubscriptionRepository {

    private val cacheMaxAge = 2.hours

    override suspend fun getSubscriptions(memberId: String, forceRefresh: Boolean): Result<List<Subscription>> {
        // Try to get cached data first (offline-first approach)
        if (!forceRefresh) {
            val cached = subscriptionDao.getByMemberId(memberId)
            if (cached.isNotEmpty()) {
                return Result.success(cached)
            }
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            // Offline: return cached data
            val cached = subscriptionDao.getByMemberId(memberId)
            return if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No internet connection and no cached data available"))
            }
        }

        // Fetch from network
        return when (val result = apiService.getMemberSubscriptions(memberId, size = 100)) {
            is ApiResult.Success -> {
                val subscriptions = result.data.content.map { it.toDomain() }
                // Save to cache
                subscriptionDao.saveAll(subscriptions)
                Result.success(subscriptions)
            }
            is ApiResult.Error -> {
                // Network error: try to return cached data as fallback
                val cached = subscriptionDao.getByMemberId(memberId)
                if (cached.isNotEmpty()) {
                    Result.success(cached)
                } else {
                    Result.failure(result.error)
                }
            }
        }
    }

    override suspend fun getSubscriptionById(subscriptionId: String): Result<Subscription> {
        // Check cache first
        val cached = subscriptionDao.getById(subscriptionId)
        if (cached != null) {
            return Result.success(cached)
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection and no cached data available"))
        }

        // Fetch from network
        return when (val result = apiService.getSubscriptionById(subscriptionId)) {
            is ApiResult.Success -> {
                val subscription = result.data.toDomain()
                // Save to cache
                subscriptionDao.save(subscription)
                Result.success(subscription)
            }
            is ApiResult.Error -> {
                Result.failure(result.error)
            }
        }
    }

    override suspend fun renewSubscription(
        subscriptionId: String,
        planId: String,
        startDate: String,
        autoRenew: Boolean
    ): Result<Subscription> {
        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection. Renewal requires online access."))
        }

        val request = RenewSubscriptionRequest(
            planId = planId,
            startDate = startDate,
            autoRenew = autoRenew
        )

        // Renew subscription
        return when (val result = apiService.renewSubscription(subscriptionId, request)) {
            is ApiResult.Success -> {
                val subscription = result.data.toDomain()
                // Save to cache
                subscriptionDao.save(subscription)
                Result.success(subscription)
            }
            is ApiResult.Error -> {
                Result.failure(result.error)
            }
        }
    }

    override suspend fun getActiveSubscriptions(memberId: String): Result<List<Subscription>> {
        // Try cache first
        val cached = subscriptionDao.getActiveByMemberId(memberId)
        if (cached.isNotEmpty()) {
            return Result.success(cached)
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection and no cached data available"))
        }

        // Fetch from network and filter active
        return when (val result = apiService.getMemberSubscriptions(memberId, size = 100)) {
            is ApiResult.Success -> {
                val subscriptions = result.data.content.map { it.toDomain() }
                // Save to cache
                subscriptionDao.saveAll(subscriptions)
                // Return only active
                val active = subscriptions.filter { it.isActive() }
                Result.success(active)
            }
            is ApiResult.Error -> {
                Result.failure(result.error)
            }
        }
    }

    override fun observeSubscriptions(memberId: String): Flow<List<Subscription>> {
        return subscriptionDao.observeByMemberId(memberId)
    }

    override suspend fun clearCache() {
        subscriptionDao.clearAll()
    }
}
