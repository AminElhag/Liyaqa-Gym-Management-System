package com.liyaqa.gym.domain.usecases

import com.liyaqa.gym.domain.validation.EmailValidator
import com.liyaqa.gym.domain.validation.NationalIdValidator
import com.liyaqa.gym.domain.validation.PhoneValidator
import com.liyaqa.gym.domain.validation.ValidationResult
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.models.LoginResponse
import com.liyaqa.gym.network.services.AuthApiService

/**
 * Use case for user registration
 * Encapsulates the business logic for creating a new user account
 */
class RegisterUseCase(
    private val authApiService: AuthApiService
) {
    /**
     * Execute the register use case
     * @param name User's full name
     * @param email User's email address
     * @param phone User's phone number
     * @param password User's password
     * @param nationalId User's national ID
     * @return Result containing LoginResponse (auto-login after registration) or error
     */
    suspend operator fun invoke(
        name: String,
        email: String,
        phone: String,
        password: String,
        nationalId: String
    ): Result<LoginResponse> {
        // Validate inputs
        val validationError = validateInputs(name, email, phone, password, nationalId)
        if (validationError != null) {
            return Result.failure(Exception(validationError))
        }

        return try {
            when (val result = authApiService.register(name, email, phone, password, nationalId)) {
                is ApiResult.Success -> Result.success(result.data)
                is ApiResult.Error -> Result.failure(
                    Exception(result.message ?: "Registration failed")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate registration inputs
     * @return Error message if validation fails, null otherwise
     */
    private fun validateInputs(
        name: String,
        email: String,
        phone: String,
        password: String,
        nationalId: String
    ): String? {
        // Validate name
        if (name.isBlank()) {
            return "Name cannot be empty"
        }
        if (name.length < 2) {
            return "Name must be at least 2 characters"
        }

        // Validate email
        when (val emailValidation = EmailValidator.validate(email)) {
            is ValidationResult.Invalid -> return emailValidation.message
            else -> {}
        }

        // Validate phone
        when (val phoneValidation = PhoneValidator.validateSaudi(phone)) {
            is ValidationResult.Invalid -> return phoneValidation.message
            else -> {}
        }

        // Validate password
        if (password.length < 8) {
            return "Password must be at least 8 characters"
        }

        // Validate national ID
        when (val nationalIdValidation = NationalIdValidator.validate(nationalId)) {
            is ValidationResult.Invalid -> return nationalIdValidation.message
            else -> {}
        }

        return null
    }
}
