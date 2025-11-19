package com.liyaqa.gym.application.member.commands

import com.liyaqa.gym.domain.entities.Gender
import java.time.LocalDate
import java.util.UUID

/**
 * Command to update a member's profile information.
 * Only non-null fields will be updated.
 *
 * @property memberId The ID of the member to update
 * @property name New member name (optional)
 * @property nameArabic New Arabic name (optional)
 * @property email New email address (optional, must be unique)
 * @property phone New phone number (optional)
 * @property nationalId New national ID (optional)
 * @property gender New gender (optional)
 * @property dateOfBirth New date of birth (optional)
 * @property emergencyContactName New emergency contact name (optional)
 * @property emergencyContactPhone New emergency contact phone (optional)
 * @property profilePhotoUrl New profile photo URL (optional)
 * @property notes New notes (optional)
 */
data class UpdateMemberCommand(
    val memberId: UUID,
    val name: String? = null,
    val nameArabic: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val nationalId: String? = null,
    val gender: Gender? = null,
    val dateOfBirth: LocalDate? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val profilePhotoUrl: String? = null,
    val notes: String? = null
) {
    init {
        // Ensure at least one field is being updated
        val hasUpdate = listOfNotNull(
            name, nameArabic, email, phone, nationalId, gender,
            dateOfBirth, emergencyContactName, emergencyContactPhone,
            profilePhotoUrl, notes
        ).isNotEmpty()

        require(hasUpdate) { "At least one field must be provided for update" }

        // Validate non-blank constraints
        name?.let { require(it.isNotBlank()) { "Name cannot be blank" } }
        email?.let { require(it.isNotBlank()) { "Email cannot be blank" } }
        phone?.let { require(it.isNotBlank()) { "Phone cannot be blank" } }
    }
}
