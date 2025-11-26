package com.liyaqa.gym.domain.services

/**
 * Generic cache service interface.
 * Provides type-safe caching operations with get, set, and delete.
 */
interface CacheService {

    /**
     * Get a value from cache.
     *
     * @param key The cache key
     * @return The cached value or null if not found
     */
    fun <T> get(key: String): T?

    /**
     * Set a value in cache.
     *
     * @param key The cache key
     * @param value The value to cache
     * @param ttlSeconds Time to live in seconds (default: 3600 = 1 hour)
     */
    fun <T> set(key: String, value: T, ttlSeconds: Long = 3600)

    /**
     * Delete a value from cache.
     *
     * @param key The cache key
     */
    fun delete(key: String)

    /**
     * Delete all values matching a pattern.
     *
     * @param pattern The key pattern (e.g., "tenant:*")
     */
    fun deleteByPattern(pattern: String)

    /**
     * Check if a key exists in cache.
     *
     * @param key The cache key
     * @return true if the key exists, false otherwise
     */
    fun exists(key: String): Boolean
}
