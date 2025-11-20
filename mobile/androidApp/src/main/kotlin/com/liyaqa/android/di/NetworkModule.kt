package com.liyaqa.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for network-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrl(): String {
        // TODO: Replace with actual API base URL from BuildConfig or environment
        return "https://api.liyaqa.com/v1/"
    }

    // TODO: Add network dependencies when implementing API client
    // - OkHttpClient with interceptors
    // - Retrofit instance
    // - API service interfaces
    // - Network status checker
}
