package com.liyaqa.gym.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * GymClass entity representing a fitness class offering.
 * Belongs to a Branch.
 */
@Serializable
data class GymClass(
    val id: String,
    val branchId: String,
    val name: String,
    val nameArabic: String? = null,
    val description: String? = null,
    val type: ClassType,
    val level: ClassLevel,
    val capacity: Int,
    val durationMinutes: Int,
    val genderRestriction: Gender? = null,
    val imageUrl: String? = null,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun canMemberJoin(memberGender: Gender): Boolean {
        return genderRestriction == null || genderRestriction == memberGender
    }

    fun displayName(): String {
        return nameArabic?.let { "$name ($it)" } ?: name
    }

    fun durationInHours(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun levelDescription(): String {
        return when (level) {
            ClassLevel.BEGINNER -> "Beginner Friendly"
            ClassLevel.INTERMEDIATE -> "Intermediate"
            ClassLevel.ADVANCED -> "Advanced"
            ClassLevel.ALL_LEVELS -> "All Levels"
        }
    }
}
