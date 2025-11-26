package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.ContactInfo
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Member entity representing a gym member.
 * Belongs to a Branch, Organization, and Tenant.
 */
data class Member(
    val id: UUID,
    val tenantId: UUID, // For direct tenant filtering
    val organizationId: UUID,
    val branchId: UUID,
    val name: String,
    val nameArabic: String?,
    val contactInfo: ContactInfo,
    val nationalId: String?,
    val gender: Gender,
    val dateOfBirth: LocalDate?,
    val status: MemberStatus,
    val profilePhotoUrl: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Member name cannot be blank" }
        nationalId?.let {
            require(it.isNotBlank()) { "National ID cannot be blank if provided" }
        }
    }

    fun activate(): Member {
        return copy(status = MemberStatus.ACTIVE, updatedAt = Instant.now())
    }

    fun suspend(): Member {
        return copy(status = MemberStatus.SUSPENDED, updatedAt = Instant.now())
    }

    fun deactivate(): Member {
        return copy(status = MemberStatus.INACTIVE, updatedAt = Instant.now())
    }

    fun updateContactInfo(newContactInfo: ContactInfo): Member {
        return copy(contactInfo = newContactInfo, updatedAt = Instant.now())
    }

    fun isActive(): Boolean = status == MemberStatus.ACTIVE

    fun getAge(): Int? {
        return dateOfBirth?.let {
            val today = LocalDate.now()
            today.year - it.year - if (today.dayOfYear < it.dayOfYear) 1 else 0
        }
    }

    companion object {
        fun create(
            tenantId: UUID,
            organizationId: UUID,
            branchId: UUID,
            name: String,
            nameArabic: String?,
            contactInfo: ContactInfo,
            nationalId: String?,
            gender: Gender,
            dateOfBirth: LocalDate?
        ): Member {
            val now = Instant.now()
            return Member(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                organizationId = organizationId,
                branchId = branchId,
                name = name,
                nameArabic = nameArabic,
                contactInfo = contactInfo,
                nationalId = nationalId,
                gender = gender,
                dateOfBirth = dateOfBirth,
                status = MemberStatus.ACTIVE,
                profilePhotoUrl = null,
                emergencyContactName = null,
                emergencyContactPhone = null,
                notes = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Member status enumeration
 */
enum class MemberStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING_APPROVAL
}
