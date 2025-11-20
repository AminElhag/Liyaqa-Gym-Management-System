package com.liyaqa.gym.domain.state

import com.liyaqa.gym.domain.Subscription

/**
 * UI state for subscriptions screen
 */
sealed class SubscriptionState {
    /**
     * Initial state
     */
    data object Idle : SubscriptionState()

    /**
     * Loading subscriptions
     */
    data object Loading : SubscriptionState()

    /**
     * Successfully loaded subscriptions
     */
    data class Success(
        val activeSubscriptions: List<Subscription>,
        val pastSubscriptions: List<Subscription>,
        val isRefreshing: Boolean = false
    ) : SubscriptionState()

    /**
     * Error loading subscriptions
     */
    data class Error(
        val message: String,
        val error: Throwable? = null
    ) : SubscriptionState()

    /**
     * No subscriptions found
     */
    data object Empty : SubscriptionState()

    /**
     * Renewing a subscription
     */
    data class Renewing(
        val subscriptionId: String
    ) : SubscriptionState()

    /**
     * Renewal successful
     */
    data class RenewalSuccess(
        val subscription: Subscription
    ) : SubscriptionState()

    /**
     * Renewal failed
     */
    data class RenewalError(
        val subscriptionId: String,
        val message: String,
        val error: Throwable? = null
    ) : SubscriptionState()
}
