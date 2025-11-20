package com.liyaqa.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liyaqa.gym.domain.usecases.LoginUseCase
import com.liyaqa.gym.domain.validation.EmailValidator
import com.liyaqa.gym.network.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Login screen
 * Manages login state and handles authentication logic
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    /**
     * Update email value
     */
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _emailError.value = null
    }

    /**
     * Update password value
     */
    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = null
    }

    /**
     * Validate form inputs
     * @return true if valid, false otherwise
     */
    private fun validateForm(): Boolean {
        var isValid = true

        // Validate email
        val emailValidation = EmailValidator.validate(_email.value)
        if (emailValidation.isInvalid()) {
            _emailError.value = emailValidation.getError()
            isValid = false
        }

        // Validate password
        if (_password.value.isBlank()) {
            _passwordError.value = "Password cannot be empty"
            isValid = false
        } else if (_password.value.length < 8) {
            _passwordError.value = "Password must be at least 8 characters"
            isValid = false
        }

        return isValid
    }

    /**
     * Perform login
     */
    fun login() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            loginUseCase(_email.value.trim(), _password.value)
                .onSuccess { loginResponse ->
                    // Save tokens
                    tokenStorage.saveAccessToken(loginResponse.accessToken)
                    tokenStorage.saveRefreshToken(loginResponse.refreshToken)

                    // Calculate expiration time
                    val expiresAt = System.currentTimeMillis() + (loginResponse.expiresIn * 1000)
                    tokenStorage.saveTokenExpiration(expiresAt)

                    _loginState.value = LoginState.Success
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Error(
                        error.message ?: "Login failed. Please try again."
                    )
                }
        }
    }

    /**
     * Reset login state to idle
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

/**
 * Sealed class representing different login states
 */
sealed class LoginState {
    /**
     * Initial state, no action taken
     */
    data object Idle : LoginState()

    /**
     * Login in progress
     */
    data object Loading : LoginState()

    /**
     * Login successful
     */
    data object Success : LoginState()

    /**
     * Login failed with error
     */
    data class Error(val message: String) : LoginState()
}
