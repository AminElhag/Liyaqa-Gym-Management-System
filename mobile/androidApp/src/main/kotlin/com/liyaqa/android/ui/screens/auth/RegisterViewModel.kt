package com.liyaqa.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liyaqa.gym.domain.usecases.RegisterUseCase
import com.liyaqa.gym.domain.validation.EmailValidator
import com.liyaqa.gym.domain.validation.NationalIdValidator
import com.liyaqa.gym.domain.validation.PhoneValidator
import com.liyaqa.gym.network.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Register screen
 * Manages registration state and handles user registration logic
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _nationalId = MutableStateFlow("")
    val nationalId: StateFlow<String> = _nationalId.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _acceptedTerms = MutableStateFlow(false)
    val acceptedTerms: StateFlow<Boolean> = _acceptedTerms.asStateFlow()

    // Error states
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()

    private val _nationalIdError = MutableStateFlow<String?>(null)
    val nationalIdError: StateFlow<String?> = _nationalIdError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    /**
     * Update field values
     */
    fun onNameChange(newName: String) {
        _name.value = newName
        _nameError.value = null
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _emailError.value = null
    }

    fun onPhoneChange(newPhone: String) {
        _phone.value = newPhone
        _phoneError.value = null
    }

    fun onNationalIdChange(newNationalId: String) {
        _nationalId.value = newNationalId
        _nationalIdError.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = null
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        _confirmPasswordError.value = null
    }

    fun onAcceptTermsChange(accepted: Boolean) {
        _acceptedTerms.value = accepted
    }

    /**
     * Validate form inputs
     * @return true if valid, false otherwise
     */
    private fun validateForm(): Boolean {
        var isValid = true

        // Validate name
        if (_name.value.isBlank()) {
            _nameError.value = "Name cannot be empty"
            isValid = false
        } else if (_name.value.length < 2) {
            _nameError.value = "Name must be at least 2 characters"
            isValid = false
        }

        // Validate email
        val emailValidation = EmailValidator.validate(_email.value)
        if (emailValidation.isInvalid()) {
            _emailError.value = emailValidation.getError()
            isValid = false
        }

        // Validate phone
        val phoneValidation = PhoneValidator.validateSaudi(_phone.value)
        if (phoneValidation.isInvalid()) {
            _phoneError.value = phoneValidation.getError()
            isValid = false
        }

        // Validate national ID
        val nationalIdValidation = NationalIdValidator.validate(_nationalId.value)
        if (nationalIdValidation.isInvalid()) {
            _nationalIdError.value = nationalIdValidation.getError()
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

        // Validate confirm password
        if (_confirmPassword.value != _password.value) {
            _confirmPasswordError.value = "Passwords do not match"
            isValid = false
        }

        // Validate terms acceptance
        if (!_acceptedTerms.value) {
            _registerState.value = RegisterState.Error("Please accept the terms and conditions")
            isValid = false
        }

        return isValid
    }

    /**
     * Perform registration
     */
    fun register() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            registerUseCase(
                name = _name.value.trim(),
                email = _email.value.trim(),
                phone = _phone.value.trim(),
                password = _password.value,
                nationalId = _nationalId.value.trim()
            )
                .onSuccess { loginResponse ->
                    // Save tokens (auto-login after registration)
                    tokenStorage.saveAccessToken(loginResponse.accessToken)
                    tokenStorage.saveRefreshToken(loginResponse.refreshToken)

                    // Calculate expiration time
                    val expiresAt = System.currentTimeMillis() + (loginResponse.expiresIn * 1000)
                    tokenStorage.saveTokenExpiration(expiresAt)

                    // Save member ID
                    tokenStorage.saveMemberId(loginResponse.member.id)

                    _registerState.value = RegisterState.Success
                }
                .onFailure { error ->
                    _registerState.value = RegisterState.Error(
                        error.message ?: "Registration failed. Please try again."
                    )
                }
        }
    }

    /**
     * Reset registration state to idle
     */
    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

/**
 * Sealed class representing different registration states
 */
sealed class RegisterState {
    /**
     * Initial state, no action taken
     */
    data object Idle : RegisterState()

    /**
     * Registration in progress
     */
    data object Loading : RegisterState()

    /**
     * Registration successful
     */
    data object Success : RegisterState()

    /**
     * Registration failed with error
     */
    data class Error(val message: String) : RegisterState()
}
