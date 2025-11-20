package com.liyaqa.gym.network.models

import kotlinx.serialization.Serializable

/**
 * Access control request/response models
 */

@Serializable
data class CheckInRequest(
    val memberId: String,
    val branchId: String,
    val notes: String? = null
)

@Serializable
data class CheckInResponse(
    val id: String,
    val memberId: String,
    val memberName: String,
    val branchId: String,
    val checkInTime: String,
    val hasActiveSubscription: Boolean,
    val subscriptionEndDate: String? = null,
    val message: String? = null
)

@Serializable
data class CheckOutRequest(
    val accessLogId: String
)

@Serializable
data class CheckOutResponse(
    val id: String,
    val memberId: String,
    val checkInTime: String,
    val checkOutTime: String,
    val durationMinutes: Int
)

@Serializable
data class AccessLogResponse(
    val id: String,
    val memberId: String,
    val memberName: String,
    val branchId: String,
    val checkInTime: String,
    val checkOutTime: String? = null,
    val durationMinutes: Int? = null,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class AccessLogListResponse(
    val content: List<AccessLogResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)
