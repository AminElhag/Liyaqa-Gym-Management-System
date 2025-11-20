package com.liyaqa.gym.database

import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Extension functions for database operations
 */

/**
 * Check if cached data is expired based on cache time and TTL
 */
fun isCacheExpired(cachedAt: String, ttl: Duration): Boolean {
    return try {
        val cached = Instant.parse(cachedAt)
        val now = kotlinx.datetime.Clock.System.now()
        val age = now - cached
        age > ttl
    } catch (e: Exception) {
        // If we can't parse the timestamp, consider it expired
        true
    }
}

/**
 * Check if cached data is still fresh
 */
fun isCacheFresh(cachedAt: String, ttl: Duration): Boolean {
    return !isCacheExpired(cachedAt, ttl)
}

/**
 * Convert Instant to database string format
 */
fun Instant.toDatabaseString(): String = this.toString()

/**
 * Parse database string to Instant
 * Returns null if parsing fails
 */
fun String.toDatabaseInstant(): Instant? {
    return try {
        Instant.parse(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * Get current timestamp as database string
 */
fun nowAsDatabaseString(): String = kotlinx.datetime.Clock.System.now().toString()

/**
 * Extension to safely convert Long to Int for database values
 */
fun Long.toIntSafe(): Int = this.toInt()

/**
 * Extension to safely convert Int to Long for database values
 */
fun Int.toLongSafe(): Long = this.toLong()

/**
 * Extension to convert Boolean to SQLite integer (0 or 1)
 */
fun Boolean.toSqliteInt(): Long = if (this) 1L else 0L

/**
 * Extension to convert SQLite integer to Boolean
 */
fun Long.toBoolean(): Boolean = this != 0L
