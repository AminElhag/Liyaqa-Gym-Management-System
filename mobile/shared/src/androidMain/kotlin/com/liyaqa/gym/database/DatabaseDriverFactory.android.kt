package com.liyaqa.gym.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.liyaqa.gym.database.LiyaqaDatabase

/**
 * Android implementation of DatabaseDriverFactory
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = LiyaqaDatabase.Schema,
            context = context,
            name = "liyaqa.db"
        )
    }
}
