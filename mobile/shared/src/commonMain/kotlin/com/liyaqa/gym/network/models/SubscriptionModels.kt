package com.liyaqa.gym.network.models

import com.liyaqa.gym.domain.SubscriptionStatus
import kotlinx.serialization.Serializable

/**
 * Subscription-related request/response models
 */

@Serializable
data class SubscriptionResponse(
    val id: String,
    val memberId: String,
    val planId: String,
    val planName: String,
    val planDescription: String? = null,
    val startDate: String,
    val endDate: String? = null,
    val status: SubscriptionStatus,
    val autoRenew: Boolean,
    val remainingVisits: Int? = null,
    val pausedAt: String? = null,
    val pausedUntil: String? = null,
    val cancelledAt: String? = null,
    val cancellationReason: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class SubscriptionListResponse(
    val content: List<SubscriptionResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)

@Serializable
data class RenewSubscriptionRequest(
    val planId: String,
    val startDate: String,
    val autoRenew: Boolean = false
)

@Serializable
data class CreateSubscriptionRequest(
    val memberId: String,
    val planId: String,
    val startDate: String,
    val autoRenew: Boolean = false
)

@Serializable
data class PauseSubscriptionRequest(
    val pauseUntil: String,
    val reason: String? = null
)

@Serializable
data class CancelSubscriptionRequest(
    val reason: String? = null
)
