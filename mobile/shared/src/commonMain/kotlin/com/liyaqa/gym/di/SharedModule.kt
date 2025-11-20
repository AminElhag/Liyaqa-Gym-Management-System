package com.liyaqa.gym.di

import com.liyaqa.gym.data.repositories.*
import com.liyaqa.gym.database.DatabaseDriverFactory
import com.liyaqa.gym.database.LiyaqaDatabase
import com.liyaqa.gym.database.LiyaqaDatabaseWrapper
import com.liyaqa.gym.database.dao.*
import com.liyaqa.gym.domain.di.domainModule
import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.ConnectivityMonitor
import com.liyaqa.gym.network.DefaultConnectivityMonitor
import com.liyaqa.gym.network.HttpClientFactory
import com.liyaqa.gym.network.KtorApiClient
import com.liyaqa.gym.network.services.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Shared Koin DI module for the mobile application
 */
fun sharedModule(enableLogging: Boolean = true) = module {
    // Include domain module
    includes(domainModule)
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

    single<SubscriptionDao> {
        SubscriptionDaoImpl(get())
    }

    single<PaymentDao> {
        PaymentDaoImpl(get())
    }

    single<InvoiceDao> {
        InvoiceDaoImpl(get())
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

    // Connectivity Monitor
    single<ConnectivityMonitor> {
        DefaultConnectivityMonitor()
    }

    // API Services
    single<MemberApiService> {
        MemberApiServiceImpl(get())
    }

    single<SubscriptionApiService> {
        SubscriptionApiServiceImpl(get())
    }

    single<ClassApiService> {
        ClassApiServiceImpl(get())
    }

    single<PaymentApiService> {
        PaymentApiServiceImpl(get())
    }

    // Repositories
    single<MemberRepository> {
        MemberRepositoryImpl(
            apiService = get(),
            memberDao = get(),
            connectivityMonitor = get()
        )
    }

    single<ScheduleRepository> {
        ScheduleRepositoryImpl(
            apiService = get(),
            scheduleDao = get(),
            connectivityMonitor = get()
        )
    }

    single<BookingRepository> {
        BookingRepositoryImpl(
            apiService = get(),
            bookingDao = get(),
            connectivityMonitor = get()
        )
    }

    single<SubscriptionRepository> {
        SubscriptionRepositoryImpl(
            apiService = get(),
            subscriptionDao = get(),
            connectivityMonitor = get()
        )
    }

    single<PaymentRepository> {
        PaymentRepositoryImpl(
            apiService = get(),
            paymentDao = get(),
            invoiceDao = get(),
            connectivityMonitor = get()
        )
    }
}

/**
 * Platform-specific module (to be provided by Android/iOS)
 * Should include:
 * - DatabaseDriverFactory
 * - Platform-specific utilities
 */
expect val platformModule: Module
