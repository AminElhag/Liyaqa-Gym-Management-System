package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.ContactInfo
import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Trainer entity representing a fitness instructor/trainer.
 * Belongs to a Branch.
 */
data class Trainer(
    val id: UUID,
    val branchId: UUID,
    val name: String,
    val nameArabic: String?,
    val contactInfo: ContactInfo,
    val specializations: Set<ClassType>,
    val certifications: List<Certification>,
    val hourlyRate: Money,
    val biography: String?,
    val photoUrl: String?,
    val hireDate: LocalDate,
    val status: TrainerStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Trainer name cannot be blank" }
        require(specializations.isNotEmpty()) { "Trainer must have at least one specialization" }
        require(hourlyRate.isPositive()) { "Hourly rate must be positive" }
    }

    fun isActive(): Boolean = status == TrainerStatus.ACTIVE

    fun canTeach(classType: ClassType): Boolean {
        return specializations.contains(classType)
    }

    fun addSpecialization(classType: ClassType): Trainer {
        return copy(
            specializations = specializations + classType,
            updatedAt = Instant.now()
        )
    }

    fun removeSpecialization(classType: ClassType): Trainer {
        require(specializations.size > 1) {
            "Cannot remove last specialization. Trainer must have at least one."
        }
        return copy(
            specializations = specializations - classType,
            updatedAt = Instant.now()
        )
    }

    fun addCertification(certification: Certification): Trainer {
        return copy(
            certifications = certifications + certification,
            updatedAt = Instant.now()
        )
    }

    fun updateHourlyRate(newRate: Money): Trainer {
        require(newRate.isPositive()) { "Hourly rate must be positive" }
        return copy(hourlyRate = newRate, updatedAt = Instant.now())
    }

    fun activate(): Trainer {
        return copy(status = TrainerStatus.ACTIVE, updatedAt = Instant.now())
    }

    fun deactivate(): Trainer {
        return copy(status = TrainerStatus.INACTIVE, updatedAt = Instant.now())
    }

    fun markOnLeave(): Trainer {
        return copy(status = TrainerStatus.ON_LEAVE, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            branchId: UUID,
            name: String,
            nameArabic: String?,
            contactInfo: ContactInfo,
            specializations: Set<ClassType>,
            hourlyRate: Money,
            hireDate: LocalDate = LocalDate.now(),
            certifications: List<Certification> = emptyList()
        ): Trainer {
            val now = Instant.now()
            return Trainer(
                id = UUID.randomUUID(),
                branchId = branchId,
                name = name,
                nameArabic = nameArabic,
                contactInfo = contactInfo,
                specializations = specializations,
                certifications = certifications,
                hourlyRate = hourlyRate,
                biography = null,
                photoUrl = null,
                hireDate = hireDate,
                status = TrainerStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Trainer certification
 */
data class Certification(
    val name: String,
    val issuingOrganization: String,
    val issueDate: LocalDate,
    val expiryDate: LocalDate?,
    val certificateNumber: String?
) {
    init {
        require(name.isNotBlank()) { "Certification name cannot be blank" }
        require(issuingOrganization.isNotBlank()) { "Issuing organization cannot be blank" }
        expiryDate?.let {
            require(!it.isBefore(issueDate)) {
                "Expiry date cannot be before issue date"
            }
        }
    }

    fun isValid(): Boolean {
        return expiryDate?.isAfter(LocalDate.now()) ?: true
    }

    fun isExpired(): Boolean {
        return expiryDate?.isBefore(LocalDate.now()) ?: false
    }
}

/**
 * Trainer status enumeration
 */
enum class TrainerStatus {
    ACTIVE,
    INACTIVE,
    ON_LEAVE
}
