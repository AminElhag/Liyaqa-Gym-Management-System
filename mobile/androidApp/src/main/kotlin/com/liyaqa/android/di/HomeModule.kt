package com.liyaqa.android.di

import android.content.Context
import com.liyaqa.gym.data.repositories.BookingRepository
import com.liyaqa.gym.data.repositories.BookingRepositoryImpl
import com.liyaqa.gym.data.repositories.MemberRepository
import com.liyaqa.gym.data.repositories.MemberRepositoryImpl
import com.liyaqa.gym.data.repositories.ScheduleRepository
import com.liyaqa.gym.data.repositories.ScheduleRepositoryImpl
import com.liyaqa.gym.data.repositories.SubscriptionRepository
import com.liyaqa.gym.data.repositories.SubscriptionRepositoryImpl
import com.liyaqa.gym.database.DatabaseDriverFactory
import com.liyaqa.gym.database.LiyaqaDatabase
import com.liyaqa.gym.database.LiyaqaDatabaseWrapper
import com.liyaqa.gym.database.dao.BookingDao
import com.liyaqa.gym.database.dao.BookingDaoImpl
import com.liyaqa.gym.database.dao.MemberDao
import com.liyaqa.gym.database.dao.MemberDaoImpl
import com.liyaqa.gym.database.dao.ScheduleDao
import com.liyaqa.gym.database.dao.ScheduleDaoImpl
import com.liyaqa.gym.database.dao.SubscriptionDao
import com.liyaqa.gym.database.dao.SubscriptionDaoImpl
import com.liyaqa.gym.domain.usecases.BookClassUseCase
import com.liyaqa.gym.domain.usecases.CancelBookingUseCase
import com.liyaqa.gym.domain.usecases.CheckInUseCase
import com.liyaqa.gym.domain.usecases.GetMemberProfileUseCase
import com.liyaqa.gym.domain.usecases.GetMyBookingsUseCase
import com.liyaqa.gym.domain.usecases.GetSchedulesUseCase
import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.ConnectivityMonitor
import com.liyaqa.gym.network.DefaultConnectivityMonitor
import com.liyaqa.gym.network.services.AccessApiService
import com.liyaqa.gym.network.services.AccessApiServiceImpl
import com.liyaqa.gym.network.services.ClassApiService
import com.liyaqa.gym.network.services.ClassApiServiceImpl
import com.liyaqa.gym.network.services.MemberApiService
import com.liyaqa.gym.network.services.MemberApiServiceImpl
import com.liyaqa.gym.network.services.SubscriptionApiService
import com.liyaqa.gym.network.services.SubscriptionApiServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for home screen dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    @Singleton
    fun provideMemberApiService(
        apiClient: ApiClient
    ): MemberApiService {
        return MemberApiServiceImpl(apiClient)
    }

    @Provides
    @Singleton
    fun provideClassApiService(
        apiClient: ApiClient
    ): ClassApiService {
        return ClassApiServiceImpl(apiClient)
    }

    @Provides
    @Singleton
    fun provideAccessApiService(
        apiClient: ApiClient
    ): AccessApiService {
        return AccessApiServiceImpl(apiClient)
    }

    @Provides
    @Singleton
    fun provideSubscriptionApiService(
        apiClient: ApiClient
    ): SubscriptionApiService {
        return SubscriptionApiServiceImpl(apiClient)
    }

    // Database dependencies
    @Provides
    @Singleton
    fun provideDatabaseDriverFactory(
        @ApplicationContext context: Context
    ): DatabaseDriverFactory {
        return DatabaseDriverFactory(context)
    }

    @Provides
    @Singleton
    fun provideLiyaqaDatabaseWrapper(
        driverFactory: DatabaseDriverFactory
    ): LiyaqaDatabaseWrapper {
        return LiyaqaDatabaseWrapper(driverFactory)
    }

    @Provides
    @Singleton
    fun provideLiyaqaDatabase(
        databaseWrapper: LiyaqaDatabaseWrapper
    ): LiyaqaDatabase {
        return databaseWrapper.database
    }

    // DAO providers
    @Provides
    @Singleton
    fun provideMemberDao(
        database: LiyaqaDatabase
    ): MemberDao {
        return MemberDaoImpl(database)
    }

    @Provides
    @Singleton
    fun provideBookingDao(
        database: LiyaqaDatabase
    ): BookingDao {
        return BookingDaoImpl(database)
    }

    @Provides
    @Singleton
    fun provideScheduleDao(
        database: LiyaqaDatabase
    ): ScheduleDao {
        return ScheduleDaoImpl(database)
    }

    // Network monitoring
    @Provides
    @Singleton
    fun provideConnectivityMonitor(): ConnectivityMonitor {
        return DefaultConnectivityMonitor()
    }

    // Repository providers
    @Provides
    @Singleton
    fun provideMemberRepository(
        memberApiService: MemberApiService,
        memberDao: MemberDao,
        connectivityMonitor: ConnectivityMonitor
    ): MemberRepository {
        return MemberRepositoryImpl(memberApiService, memberDao, connectivityMonitor)
    }

    @Provides
    @Singleton
    fun provideBookingRepository(
        classApiService: ClassApiService,
        bookingDao: BookingDao,
        connectivityMonitor: ConnectivityMonitor
    ): BookingRepository {
        return BookingRepositoryImpl(classApiService, bookingDao, connectivityMonitor)
    }

    @Provides
    @Singleton
    fun provideScheduleRepository(
        classApiService: ClassApiService,
        scheduleDao: ScheduleDao,
        connectivityMonitor: ConnectivityMonitor
    ): ScheduleRepository {
        return ScheduleRepositoryImpl(classApiService, scheduleDao, connectivityMonitor)
    }

    @Provides
    @Singleton
    fun provideGetMemberProfileUseCase(
        memberRepository: MemberRepository
    ): GetMemberProfileUseCase {
        return GetMemberProfileUseCase(memberRepository)
    }

    @Provides
    @Singleton
    fun provideGetMyBookingsUseCase(
        bookingRepository: BookingRepository
    ): GetMyBookingsUseCase {
        return GetMyBookingsUseCase(bookingRepository)
    }

    @Provides
    @Singleton
    fun provideCheckInUseCase(
        bookingRepository: BookingRepository,
        scheduleRepository: ScheduleRepository
    ): CheckInUseCase {
        return CheckInUseCase(bookingRepository, scheduleRepository)
    }

    @Provides
    @Singleton
    fun provideSubscriptionDao(
        database: LiyaqaDatabase
    ): SubscriptionDao {
        return SubscriptionDaoImpl(database)
    }

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        subscriptionApiService: SubscriptionApiService,
        subscriptionDao: SubscriptionDao,
        connectivityMonitor: ConnectivityMonitor
    ): SubscriptionRepository {
        return SubscriptionRepositoryImpl(subscriptionApiService, subscriptionDao, connectivityMonitor)
    }

    @Provides
    @Singleton
    fun provideBookClassUseCase(
        bookingRepository: BookingRepository,
        memberRepository: MemberRepository,
        scheduleRepository: ScheduleRepository,
        subscriptionRepository: SubscriptionRepository
    ): BookClassUseCase {
        return BookClassUseCase(bookingRepository, memberRepository, scheduleRepository, subscriptionRepository)
    }

    @Provides
    @Singleton
    fun provideCancelBookingUseCase(
        bookingRepository: BookingRepository,
        scheduleRepository: ScheduleRepository
    ): CancelBookingUseCase {
        return CancelBookingUseCase(bookingRepository, scheduleRepository)
    }

    @Provides
    @Singleton
    fun provideGetSchedulesUseCase(
        scheduleRepository: ScheduleRepository
    ): GetSchedulesUseCase {
        return GetSchedulesUseCase(scheduleRepository)
    }
}
