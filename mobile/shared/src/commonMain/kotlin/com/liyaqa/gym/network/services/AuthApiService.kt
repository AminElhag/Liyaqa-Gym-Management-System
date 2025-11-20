package com.liyaqa.gym.network.services

import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.ApiConfig
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.models.LoginRequest
import com.liyaqa.gym.network.models.LoginResponse
import com.liyaqa.gym.network.models.LogoutRequest
import com.liyaqa.gym.network.models.RefreshTokenRequest
import com.liyaqa.gym.network.models.RefreshTokenResponse

/**
 * API service for authentication operations
 */
interface AuthApiService {
    suspend fun login(email: String, password: String): ApiResult<LoginResponse>
    suspend fun logout(refreshToken: String): ApiResult<Unit>
    suspend fun refreshToken(refreshToken: String): ApiResult<RefreshTokenResponse>
}

/**
 * Default implementation of AuthApiService
 */
class AuthApiServiceImpl(
    private val apiClient: ApiClient
) : AuthApiService {

    override suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        val request = LoginRequest(email, password)
        return apiClient.post(ApiConfig.Endpoints.LOGIN, request)
    }

    override suspend fun logout(refreshToken: String): ApiResult<Unit> {
        val request = LogoutRequest(refreshToken)
        return apiClient.post(ApiConfig.Endpoints.LOGOUT, request)
    }

    override suspend fun refreshToken(refreshToken: String): ApiResult<RefreshTokenResponse> {
        val request = RefreshTokenRequest(refreshToken)
        return apiClient.post(ApiConfig.Endpoints.REFRESH_TOKEN, request)
    }
}
