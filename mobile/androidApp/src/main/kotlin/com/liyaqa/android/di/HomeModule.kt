package com.liyaqa.android.di

import com.liyaqa.gym.data.repositories.BookingRepository
import com.liyaqa.gym.data.repositories.MemberRepository
import com.liyaqa.gym.data.repositories.ScheduleRepository
import com.liyaqa.gym.domain.usecases.CheckInUseCase
import com.liyaqa.gym.domain.usecases.GetMemberProfileUseCase
import com.liyaqa.gym.domain.usecases.GetMyBookingsUseCase
import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.services.AccessApiService
import com.liyaqa.gym.network.services.AccessApiServiceImpl
import com.liyaqa.gym.network.services.ClassApiService
import com.liyaqa.gym.network.services.ClassApiServiceImpl
import com.liyaqa.gym.network.services.MemberApiService
import com.liyaqa.gym.network.services.MemberApiServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideMemberRepository(
        memberApiService: MemberApiService
    ): MemberRepository {
        return MemberRepository(memberApiService)
    }

    @Provides
    @Singleton
    fun provideBookingRepository(
        classApiService: ClassApiService
    ): BookingRepository {
        return BookingRepository(classApiService)
    }

    @Provides
    @Singleton
    fun provideScheduleRepository(
        classApiService: ClassApiService
    ): ScheduleRepository {
        return ScheduleRepository(classApiService)
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
        accessApiService: AccessApiService
    ): CheckInUseCase {
        return CheckInUseCase(accessApiService)
    }
}
