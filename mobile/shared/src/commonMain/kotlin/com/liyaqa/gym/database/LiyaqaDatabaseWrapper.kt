package com.liyaqa.gym.database

import app.cash.sqldelight.db.SqlDriver
import com.liyaqa.gym.cache.isCacheValid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Database wrapper that provides access to the SQLDelight database
 * and manages cache TTL operations.
 */
class LiyaqaDatabaseWrapper(
    driverFactory: DatabaseDriverFactory
) {
    private val driver: SqlDriver = driverFactory.createDriver()
    val database: LiyaqaDatabase = LiyaqaDatabase(driver)

    companion object {
        /**
         * Default cache TTL for all entities
         */
        val DEFAULT_CACHE_TTL: Duration = 24.hours

        /**
         * Short cache TTL for frequently changing data like class schedules
         */
        val SHORT_CACHE_TTL: Duration = 1.hours

        /**
         * Long cache TTL for rarely changing data
         */
        val LONG_CACHE_TTL: Duration = 7.hours * 24 // 7 days
    }

    /**
     * Clear all stale data from the database based on TTL
     */
    fun clearStaleData(maxAge: Duration = DEFAULT_CACHE_TTL) {
        val threshold = Clock.System.now().minus(maxAge)
        clearStaleMembers(threshold)
        clearStaleSchedules(threshold)
        clearStaleBookings(threshold)
        clearStaleSubscriptions(threshold)
    }

    /**
     * Clear stale members
     */
    private fun clearStaleMembers(threshold: Instant) {
        database.memberEntityQueries.transaction {
            val allMembers = database.memberEntityQueries.selectAll().executeAsList()
            allMembers.forEach { member ->
                val cachedAt = Instant.parse(member.cachedAt)
                if (cachedAt < threshold) {
                    database.memberEntityQueries.deleteById(member.id)
                }
            }
        }
    }

    /**
     * Clear stale class schedules
     */
    private fun clearStaleSchedules(threshold: Instant) {
        database.classScheduleEntityQueries.transaction {
            val allSchedules = database.classScheduleEntityQueries.selectAll().executeAsList()
            allSchedules.forEach { schedule ->
                val cachedAt = Instant.parse(schedule.cachedAt)
                if (cachedAt < threshold) {
                    database.classScheduleEntityQueries.deleteById(schedule.id)
                }
            }
        }
    }

    /**
     * Clear stale bookings
     */
    private fun clearStaleBookings(threshold: Instant) {
        database.bookingEntityQueries.transaction {
            val allBookings = database.bookingEntityQueries.selectAll().executeAsList()
            allBookings.forEach { booking ->
                val cachedAt = Instant.parse(booking.cachedAt)
                if (cachedAt < threshold) {
                    database.bookingEntityQueries.deleteById(booking.id)
                }
            }
        }
    }

    /**
     * Clear stale subscriptions
     */
    private fun clearStaleSubscriptions(threshold: Instant) {
        database.subscriptionEntityQueries.transaction {
            val allSubscriptions = database.subscriptionEntityQueries.selectAll().executeAsList()
            allSubscriptions.forEach { subscription ->
                val cachedAt = Instant.parse(subscription.cachedAt)
                if (cachedAt < threshold) {
                    database.subscriptionEntityQueries.deleteById(subscription.id)
                }
            }
        }
    }

    /**
     * Clear all cached data
     */
    fun clearAllCache() {
        database.transaction {
            database.memberEntityQueries.deleteAll()
            database.classScheduleEntityQueries.deleteAll()
            database.bookingEntityQueries.deleteAll()
            database.subscriptionEntityQueries.deleteAll()
        }
    }

    /**
     * Close the database connection
     */
    fun close() {
        driver.close()
    }
}
