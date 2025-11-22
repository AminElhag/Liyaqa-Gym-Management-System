package com.liyaqa.android.di

import android.content.Context
import com.liyaqa.android.data.AndroidTokenStorage
import com.liyaqa.gym.domain.usecases.LoginUseCase
import com.liyaqa.gym.domain.usecases.RegisterUseCase
import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.HttpClientFactory
import com.liyaqa.gym.network.KtorApiClient
import com.liyaqa.gym.network.TokenStorage
import com.liyaqa.gym.network.services.AuthApiService
import com.liyaqa.gym.network.services.AuthApiServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

/**
 * Hilt module for authentication-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideTokenStorage(
        @ApplicationContext context: Context
    ): TokenStorage {
        return AndroidTokenStorage(context)
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        tokenStorage: TokenStorage
    ): HttpClient {
        return HttpClientFactory.create(
            enableLogging = true,
            tokenStorage = tokenStorage,
            onTokenRefreshFailed = {
                // Handle token refresh failure
                // Could trigger logout or navigate to login screen
            }
        )
    }

    @Provides
    @Singleton
    fun provideApiClient(
        httpClient: HttpClient
    ): ApiClient {
        return KtorApiClient(httpClient)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(
        apiClient: ApiClient
    ): AuthApiService {
        return AuthApiServiceImpl(apiClient)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(
        authApiService: AuthApiService
    ): LoginUseCase {
        return LoginUseCase(authApiService)
    }

    @Provides
    @Singleton
    fun provideRegisterUseCase(
        authApiService: AuthApiService
    ): RegisterUseCase {
        return RegisterUseCase(authApiService)
    }
}
