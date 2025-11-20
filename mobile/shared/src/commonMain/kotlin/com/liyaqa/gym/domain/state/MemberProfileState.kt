package com.liyaqa.gym.domain.state

import com.liyaqa.gym.domain.Member

/**
 * UI state for member profile screen
 */
sealed class MemberProfileState {
    /**
     * Initial state
     */
    data object Idle : MemberProfileState()

    /**
     * Loading member profile
     */
    data object Loading : MemberProfileState()

    /**
     * Successfully loaded member profile
     */
    data class Success(
        val member: Member,
        val isRefreshing: Boolean = false
    ) : MemberProfileState()

    /**
     * Error loading member profile
     */
    data class Error(
        val message: String,
        val error: Throwable? = null
    ) : MemberProfileState()

    /**
     * Updating member profile
     */
    data class Updating(
        val member: Member
    ) : MemberProfileState()

    /**
     * Update successful
     */
    data class UpdateSuccess(
        val member: Member
    ) : MemberProfileState()

    /**
     * Update failed
     */
    data class UpdateError(
        val member: Member,
        val message: String,
        val error: Throwable? = null
    ) : MemberProfileState()
}
