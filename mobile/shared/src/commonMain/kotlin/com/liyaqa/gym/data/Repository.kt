package com.liyaqa.gym.data

import com.liyaqa.gym.cache.CacheStrategy
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.NetworkError
import kotlinx.coroutines.flow.Flow

/**
 * Base repository interface for data operations
 */
interface Repository<T, ID> {
    /**
     * Get all items with optional caching strategy
     */
    suspend fun getAll(cacheStrategy: CacheStrategy = CacheStrategy.CacheFirst()): ApiResult<List<T>>

    /**
     * Get item by ID
     */
    suspend fun getById(id: ID, cacheStrategy: CacheStrategy = CacheStrategy.CacheFirst()): ApiResult<T>

    /**
     * Observe items as a flow (from local cache)
     */
    fun observeAll(): Flow<List<T>>

    /**
     * Observe single item by ID as a flow (from local cache)
     */
    fun observeById(id: ID): Flow<T?>

    /**
     * Refresh data from network and update cache
     */
    suspend fun refresh(): ApiResult<Unit>

    /**
     * Clear all cached data
     */
    suspend fun clearCache()
}

/**
 * Result wrapper that includes cache information
 */
sealed class DataResult<out T> {
    data class Success<T>(
        val data: T,
        val isFromCache: Boolean = false
    ) : DataResult<T>()

    data class Error(
        val error: NetworkError,
        val cachedData: Any? = null
    ) : DataResult<Nothing>()

    fun <R> map(transform: (T) -> R): DataResult<R> {
        return when (this) {
            is Success -> Success(transform(data), isFromCache)
            is Error -> Error(error, cachedData)
        }
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
}
