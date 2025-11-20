package com.liyaqa.android.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liyaqa.gym.data.repositories.MemberRepository
import com.liyaqa.gym.data.repositories.SubscriptionRepository
import com.liyaqa.gym.domain.Member
import com.liyaqa.gym.network.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Profile screen
 * Manages profile data, subscription info, and user actions
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _editProfileState = MutableStateFlow(EditProfileState())
    val editProfileState: StateFlow<EditProfileState> = _editProfileState.asStateFlow()

    private val _notificationSettingsState = MutableStateFlow(NotificationSettingsState())
    val notificationSettingsState: StateFlow<NotificationSettingsState> = _notificationSettingsState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Load member profile and subscription data
     */
    fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!forceRefresh) {
                    _profileState.value = ProfileState.Loading
                }

                // Get member ID
                val memberId = tokenStorage.getMemberId()
                if (memberId == null) {
                    _profileState.value = ProfileState.Error("User not authenticated")
                    _isRefreshing.value = false
                    return@launch
                }

                // Load member profile
                val memberResult = memberRepository.getMemberProfile(memberId, forceRefresh)
                if (memberResult.isFailure) {
                    _profileState.value = ProfileState.Error(
                        memberResult.exceptionOrNull()?.message ?: "Failed to load profile"
                    )
                    _isRefreshing.value = false
                    return@launch
                }

                val member = memberResult.getOrThrow()

                // Load active subscription
                val subscriptionResult = subscriptionRepository.getActiveSubscriptions(memberId)
                val subscription = subscriptionResult.getOrNull()?.firstOrNull()

                // Update state
                _profileState.value = ProfileState.Success(
                    member = member,
                    subscription = subscription
                )

                _isRefreshing.value = false

            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(
                    e.message ?: "An unexpected error occurred"
                )
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Refresh profile data
     */
    fun refresh() {
        _isRefreshing.value = true
        loadProfile(forceRefresh = true)
    }

    /**
     * Update member profile
     */
    fun updateProfile(
        name: String,
        phone: String,
        emergencyContactName: String?,
        emergencyContactPhone: String?,
        profilePhotoUrl: String?
    ) {
        viewModelScope.launch {
            try {
                _editProfileState.value = _editProfileState.value.copy(
                    isSaving = true,
                    error = null,
                    successMessage = null
                )

                // Get current member
                val currentState = _profileState.value
                if (currentState !is ProfileState.Success) {
                    _editProfileState.value = _editProfileState.value.copy(
                        isSaving = false,
                        error = "No profile loaded"
                    )
                    return@launch
                }

                // Create updated member
                val updatedMember = currentState.member.copy(
                    name = name,
                    contactInfo = currentState.member.contactInfo.copy(phone = phone),
                    emergencyContactName = emergencyContactName,
                    emergencyContactPhone = emergencyContactPhone,
                    profilePhotoUrl = profilePhotoUrl
                )

                // Update profile
                val result = memberRepository.updateProfile(updatedMember)
                if (result.isSuccess) {
                    // Update state with new data
                    _profileState.value = currentState.copy(member = result.getOrThrow())
                    _editProfileState.value = _editProfileState.value.copy(
                        isSaving = false,
                        successMessage = "Profile updated successfully"
                    )
                } else {
                    _editProfileState.value = _editProfileState.value.copy(
                        isSaving = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to update profile"
                    )
                }

            } catch (e: Exception) {
                _editProfileState.value = _editProfileState.value.copy(
                    isSaving = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Update notification preferences
     */
    fun updateNotificationPreferences(preferences: NotificationPreferences) {
        viewModelScope.launch {
            try {
                _notificationSettingsState.value = _notificationSettingsState.value.copy(
                    isSaving = true,
                    error = null,
                    successMessage = null
                )

                // TODO: Implement API call to save notification preferences
                // For now, just save locally
                kotlinx.coroutines.delay(500) // Simulate API call

                _notificationSettingsState.value = _notificationSettingsState.value.copy(
                    isSaving = false,
                    preferences = preferences,
                    successMessage = "Notification settings updated"
                )

            } catch (e: Exception) {
                _notificationSettingsState.value = _notificationSettingsState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to update settings"
                )
            }
        }
    }

    /**
     * Load notification preferences
     */
    fun loadNotificationPreferences() {
        viewModelScope.launch {
            try {
                _notificationSettingsState.value = _notificationSettingsState.value.copy(
                    isLoading = true,
                    error = null
                )

                // TODO: Implement API call to load notification preferences
                // For now, use default values
                kotlinx.coroutines.delay(300) // Simulate API call

                _notificationSettingsState.value = _notificationSettingsState.value.copy(
                    isLoading = false,
                    preferences = NotificationPreferences()
                )

            } catch (e: Exception) {
                _notificationSettingsState.value = _notificationSettingsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load settings"
                )
            }
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Clear tokens
                tokenStorage.clearTokens()

                // Clear cached data
                memberRepository.clearCache()
                subscriptionRepository.clearCache()

                // Navigation to login screen should be handled by the UI
                // The UI should observe a logout event or state change

            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Clear edit profile messages
     */
    fun clearEditProfileMessages() {
        _editProfileState.value = _editProfileState.value.copy(
            error = null,
            successMessage = null
        )
    }

    /**
     * Clear notification settings messages
     */
    fun clearNotificationSettingsMessages() {
        _notificationSettingsState.value = _notificationSettingsState.value.copy(
            error = null,
            successMessage = null
        )
    }
}
