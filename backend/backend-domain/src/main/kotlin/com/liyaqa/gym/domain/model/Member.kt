package com.liyaqa.gym.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Domain model representing a gym member.
 * Contains core business rules and validations.
 */
data class Member(
    val id: UUID = UUID.randomUUID(),
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val membershipType: MembershipType,
    val membershipStatus: MembershipStatus,
    val joinDate: LocalDateTime,
    val expiryDate: LocalDateTime?,
    val emergencyContact: EmergencyContact?
) {
    init {
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) { "Invalid email format" }
        require(phoneNumber.isNotBlank()) { "Phone number cannot be blank" }
    }

    val fullName: String
        get() = "$firstName $lastName"

    fun isActive(): Boolean = membershipStatus == MembershipStatus.ACTIVE

    fun isExpired(): Boolean = expiryDate?.isBefore(LocalDateTime.now()) ?: false
}

data class EmergencyContact(
    val name: String,
    val phoneNumber: String,
    val relationship: String
)

enum class MembershipType {
    BASIC,
    PREMIUM,
    VIP,
    STUDENT,
    CORPORATE
}

enum class MembershipStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    EXPIRED
}
