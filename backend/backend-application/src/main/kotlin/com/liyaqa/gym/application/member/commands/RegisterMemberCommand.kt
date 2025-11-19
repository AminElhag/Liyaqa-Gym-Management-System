package com.liyaqa.gym.application.member.commands

import com.liyaqa.gym.domain.entities.Gender
import java.time.LocalDate
import java.util.UUID

/**
 * Command to register a new member in the system.
 *
 * @property branchId The ID of the branch where the member is registering
 * @property name Member's full name
 * @property nameArabic Member's name in Arabic (optional)
 * @property email Member's email address (must be unique)
 * @property phone Member's phone number
 * @property nationalId Saudi national ID (optional, will be validated if provided)
 * @property gender Member's gender
 * @property dateOfBirth Member's date of birth (optional)
 * @property emergencyContactName Emergency contact person's name (optional)
 * @property emergencyContactPhone Emergency contact person's phone (optional)
 * @property profilePhotoUrl URL to member's profile photo (optional)
 * @property notes Additional notes about the member (optional)
 */
data class RegisterMemberCommand(
    val branchId: UUID,
    val name: String,
    val nameArabic: String? = null,
    val email: String,
    val phone: String,
    val nationalId: String? = null,
    val gender: Gender,
    val dateOfBirth: LocalDate? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val profilePhotoUrl: String? = null,
    val notes: String? = null
) {
    init {
        require(name.isNotBlank()) { "Member name is required and cannot be blank" }
        require(email.isNotBlank()) { "Email is required and cannot be blank" }
        require(phone.isNotBlank()) { "Phone is required and cannot be blank" }

        // Validate national ID format if provided
        nationalId?.let {
            require(it.isNotBlank()) { "National ID cannot be blank if provided" }
        }

        // Validate emergency contact phone if emergency contact name is provided
        if (emergencyContactName?.isNotBlank() == true) {
            require(emergencyContactPhone?.isNotBlank() == true) {
                "Emergency contact phone is required when emergency contact name is provided"
            }
        }
    }
}
