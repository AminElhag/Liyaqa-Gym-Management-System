package com.liyaqa.gym.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.liyaqa.gym.database.LiyaqaDatabase

/**
 * iOS implementation of DatabaseDriverFactory
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = LiyaqaDatabase.Schema,
            name = "liyaqa.db"
        )
    }
}
