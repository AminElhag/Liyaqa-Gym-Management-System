package com.liyaqa.gym.di

import com.liyaqa.gym.database.DatabaseDriverFactory
import com.liyaqa.gym.database.LiyaqaDatabase
import com.liyaqa.gym.database.LiyaqaDatabaseWrapper
import com.liyaqa.gym.database.dao.BookingDao
import com.liyaqa.gym.database.dao.BookingDaoImpl
import com.liyaqa.gym.database.dao.MemberDao
import com.liyaqa.gym.database.dao.MemberDaoImpl
import com.liyaqa.gym.database.dao.ScheduleDao
import com.liyaqa.gym.database.dao.ScheduleDaoImpl
import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.HttpClientFactory
import com.liyaqa.gym.network.KtorApiClient
import com.liyaqa.gym.network.services.MemberApiService
import com.liyaqa.gym.network.services.MemberApiServiceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Shared Koin DI module for the mobile application
 */
fun sharedModule(enableLogging: Boolean = true) = module {
    // Database
    single {
        val driverFactory: DatabaseDriverFactory = get()
        LiyaqaDatabaseWrapper(driverFactory)
    }

    single {
        val wrapper: LiyaqaDatabaseWrapper = get()
        wrapper.database
    }

    // DAOs
    single<MemberDao> {
        MemberDaoImpl(get())
    }

    single<ScheduleDao> {
        ScheduleDaoImpl(get())
    }

    single<BookingDao> {
        BookingDaoImpl(get())
    }

    // Network
    single {
        HttpClientFactory.create(
            enableLogging = enableLogging,
            tokenProvider = null // TODO: Implement token provider
        )
    }

    single<ApiClient> {
        KtorApiClient(get())
    }

    // API Services
    single<MemberApiService> {
        MemberApiServiceImpl(get())
    }

    // TODO: Add more services as needed
    // - SubscriptionApiService
    // - ClassApiService
    // - BookingApiService

    // TODO: Add repositories
    // - MemberRepository
    // - SubscriptionRepository
    // - ClassRepository
    // - BookingRepository
}

/**
 * Platform-specific module (to be provided by Android/iOS)
 * Should include:
 * - DatabaseDriverFactory
 * - Platform-specific utilities
 */
expect val platformModule: Module
