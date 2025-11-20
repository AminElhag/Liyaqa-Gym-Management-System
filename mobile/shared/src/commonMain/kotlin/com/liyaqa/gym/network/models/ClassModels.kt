package com.liyaqa.gym.network.models

import com.liyaqa.gym.domain.ClassLevel
import com.liyaqa.gym.domain.ClassType
import kotlinx.serialization.Serializable

/**
 * Class-related request/response models
 */

@Serializable
data class ClassResponse(
    val id: String,
    val branchId: String,
    val name: String,
    val nameArabic: String? = null,
    val description: String? = null,
    val type: ClassType,
    val level: ClassLevel,
    val durationMinutes: Int,
    val defaultCapacity: Int,
    val imageUrl: String? = null,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ScheduleResponse(
    val id: String,
    val classId: String,
    val className: String,
    val classType: ClassType,
    val classLevel: ClassLevel,
    val instructorId: String? = null,
    val instructorName: String? = null,
    val startDateTime: String,
    val endDateTime: String,
    val capacity: Int,
    val bookedCount: Int,
    val waitlistCount: Int,
    val isCancelled: Boolean,
    val cancellationReason: String? = null,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ScheduleListResponse(
    val content: List<ScheduleResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)

@Serializable
data class BookingResponse(
    val id: String,
    val memberId: String,
    val scheduleId: String,
    val schedule: ScheduleResponse? = null,
    val status: String,
    val bookedAt: String,
    val waitlistPosition: Int? = null,
    val confirmedAt: String? = null,
    val checkedInAt: String? = null,
    val cancelledAt: String? = null,
    val cancellationReason: String? = null,
    val noShowMarkedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BookingListResponse(
    val content: List<BookingResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)

@Serializable
data class CreateBookingRequest(
    val scheduleId: String,
    val notes: String? = null
)

@Serializable
data class CancelBookingRequest(
    val reason: String? = null
)
