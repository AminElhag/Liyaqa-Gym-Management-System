package com.liyaqa.android.ui.screens.profile

import com.liyaqa.gym.domain.Member
import com.liyaqa.gym.domain.Subscription

/**
 * UI State for the Profile screen
 */
sealed class ProfileState {
    data object Loading : ProfileState()
    data class Success(
        val member: Member,
        val subscription: Subscription?
    ) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

/**
 * Notification preferences model
 */
data class NotificationPreferences(
    val classReminders: Boolean = true,
    val paymentReminders: Boolean = true,
    val promotionalNotifications: Boolean = false,
    val bookingConfirmations: Boolean = true,
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val smsEnabled: Boolean = false
)

/**
 * UI State for Edit Profile screen
 */
data class EditProfileState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * UI State for Notification Settings screen
 */
data class NotificationSettingsState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val preferences: NotificationPreferences = NotificationPreferences(),
    val error: String? = null,
    val successMessage: String? = null
)
