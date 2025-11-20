package com.liyaqa.gym.cache

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Cache strategy for determining when to use cached data vs fetch from network
 */
sealed class CacheStrategy {
    /**
     * Always fetch from network, cache the result
     */
    data object NetworkFirst : CacheStrategy()

    /**
     * Use cache if available and not expired, otherwise fetch from network
     */
    data class CacheFirst(
        val maxAge: Duration = 1.hours
    ) : CacheStrategy()

    /**
     * Only use cache, never fetch from network
     */
    data object CacheOnly : CacheStrategy()

    /**
     * Only fetch from network, don't use cache
     */
    data object NetworkOnly : CacheStrategy()
}

/**
 * Helper to check if cached data is still valid
 */
fun isCacheValid(cachedAt: Instant, maxAge: Duration): Boolean {
    val now = Clock.System.now()
    val age = now - cachedAt
    return age <= maxAge
}

/**
 * Result from a cached data source
 */
sealed class CachedResult<out T> {
    data class Fresh<T>(val data: T) : CachedResult<T>()
    data class Stale<T>(val data: T) : CachedResult<T>()
    data object Missing : CachedResult<Nothing>()

    fun getOrNull(): T? = when (this) {
        is Fresh -> data
        is Stale -> data
        is Missing -> null
    }

    fun isFresh(): Boolean = this is Fresh
}
