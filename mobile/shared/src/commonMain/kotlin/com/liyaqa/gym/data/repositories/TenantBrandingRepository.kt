package com.liyaqa.gym.data.repositories

import com.liyaqa.gym.domain.TenantBranding
import com.liyaqa.gym.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

/**
 * Repository for tenant branding operations.
 * Implements caching for better performance on mobile.
 */
class TenantBrandingRepository(
    private val apiClient: ApiClient
) {

    // In-memory cache for tenant branding
    private var cachedBranding: TenantBranding? = null
    private var cacheTimestamp: Instant? = null
    private val cacheDuration = 1.hours

    /**
     * Get tenant branding by slug with caching
     */
    suspend fun getTenantBranding(tenantSlug: String, forceRefresh: Boolean = false): Result<TenantBranding> {
        return try {
            // Check cache if not forcing refresh
            if (!forceRefresh && isCacheValid()) {
                cachedBranding?.let {
                    return Result.success(it)
                }
            }

            // Fetch from API
            val response = apiClient.client.get("/api/v1/public/branding/$tenantSlug")
            val branding = response.body<TenantBranding>()

            // Update cache
            cachedBranding = branding
            cacheTimestamp = Clock.System.now()

            Result.success(branding)
        } catch (e: Exception) {
            // Return cached data if available, even if expired
            cachedBranding?.let {
                return Result.success(it)
            }
            Result.failure(e)
        }
    }

    /**
     * Observe tenant branding changes as a Flow
     */
    fun observeTenantBranding(tenantSlug: String): Flow<Result<TenantBranding>> = flow {
        // Emit cached value first if available
        cachedBranding?.let {
            emit(Result.success(it))
        }

        // Then fetch fresh data
        val result = getTenantBranding(tenantSlug, forceRefresh = false)
        emit(result)
    }

    /**
     * Clear the branding cache
     */
    fun clearCache() {
        cachedBranding = null
        cacheTimestamp = null
    }

    /**
     * Check if cache is still valid
     */
    private fun isCacheValid(): Boolean {
        val timestamp = cacheTimestamp ?: return false
        val now = Clock.System.now()
        return (now - timestamp) < cacheDuration
    }

    /**
     * Get cached branding if available
     */
    fun getCachedBranding(): TenantBranding? = cachedBranding
}
