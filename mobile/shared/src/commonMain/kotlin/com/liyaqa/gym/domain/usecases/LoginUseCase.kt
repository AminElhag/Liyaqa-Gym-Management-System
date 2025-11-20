package com.liyaqa.gym.domain.usecases

import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.models.LoginResponse
import com.liyaqa.gym.network.services.AuthApiService

/**
 * Use case for user login
 * Encapsulates the business logic for authenticating a user
 */
class LoginUseCase(
    private val authApiService: AuthApiService
) {
    /**
     * Execute the login use case
     * @param email User email
     * @param password User password
     * @return Result containing LoginResponse or error
     */
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<LoginResponse> {
        return try {
            when (val result = authApiService.login(email, password)) {
                is ApiResult.Success -> Result.success(result.data)
                is ApiResult.Error -> Result.failure(
                    Exception(result.message ?: "Login failed")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
