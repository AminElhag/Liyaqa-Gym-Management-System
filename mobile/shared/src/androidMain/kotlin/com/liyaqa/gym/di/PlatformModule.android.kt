package com.liyaqa.gym.di

import com.liyaqa.gym.database.DatabaseDriverFactory
import org.koin.dsl.module

/**
 * Android-specific Koin module
 */
actual val platformModule = module {
    single { DatabaseDriverFactory(get()) }
}
