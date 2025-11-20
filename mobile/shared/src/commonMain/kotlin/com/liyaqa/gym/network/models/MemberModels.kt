package com.liyaqa.gym.network.models

import com.liyaqa.gym.domain.Gender
import com.liyaqa.gym.domain.MemberStatus
import kotlinx.serialization.Serializable

/**
 * Member-related request/response models
 */

@Serializable
data class MemberResponse(
    val id: String,
    val branchId: String,
    val name: String,
    val nameArabic: String? = null,
    val email: String,
    val phone: String,
    val nationalId: String? = null,
    val gender: Gender,
    val dateOfBirth: String? = null,
    val status: MemberStatus,
    val profilePhotoUrl: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val nameArabic: String? = null,
    val phone: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val profilePhotoUrl: String? = null
)

@Serializable
data class MemberListResponse(
    val content: List<MemberResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)
