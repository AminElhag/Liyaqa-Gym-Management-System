package com.liyaqa.gym.domain.model

import java.time.LocalDate
import java.util.UUID

/**
 * Domain model representing a gym trainer/instructor.
 */
data class Trainer(
    val id: UUID = UUID.randomUUID(),
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val specializations: Set<ClassType>,
    val certifications: List<Certification>,
    val hireDate: LocalDate,
    val status: TrainerStatus
) {
    init {
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) { "Invalid email format" }
        require(specializations.isNotEmpty()) { "Trainer must have at least one specialization" }
    }

    val fullName: String
        get() = "$firstName $lastName"

    fun isActive(): Boolean = status == TrainerStatus.ACTIVE

    fun canTeach(classType: ClassType): Boolean = specializations.contains(classType)
}

data class Certification(
    val name: String,
    val issuingOrganization: String,
    val issueDate: LocalDate,
    val expiryDate: LocalDate?
) {
    fun isValid(): Boolean = expiryDate?.isAfter(LocalDate.now()) ?: true
}

enum class TrainerStatus {
    ACTIVE,
    ON_LEAVE,
    INACTIVE
}
