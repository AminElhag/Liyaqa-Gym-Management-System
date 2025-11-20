package com.liyaqa.gym.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific database driver factory.
 * Implementations are provided in androidMain and iosMain.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
