package com.liyaqa.gym.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Member entity representing a gym member.
 * Belongs to a Branch.
 */
@Serializable
data class Member(
    val id: String,
    val branchId: String,
    val name: String,
    val nameArabic: String? = null,
    val contactInfo: ContactInfo,
    val nationalId: String? = null,
    val gender: Gender,
    val dateOfBirth: LocalDate? = null,
    val status: MemberStatus,
    val profilePhotoUrl: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun isActive(): Boolean = status == MemberStatus.ACTIVE

    fun maskedContactInfo(): Pair<String, String> {
        return Pair(contactInfo.maskedEmail(), contactInfo.maskedPhone())
    }

    fun getAge(currentDate: LocalDate): Int? {
        return dateOfBirth?.let { dob ->
            var age = currentDate.year - dob.year
            if (currentDate.monthNumber < dob.monthNumber ||
                (currentDate.monthNumber == dob.monthNumber && currentDate.dayOfMonth < dob.dayOfMonth)
            ) {
                age--
            }
            age
        }
    }

    fun displayName(): String {
        return nameArabic?.let { "$name ($it)" } ?: name
    }
}
