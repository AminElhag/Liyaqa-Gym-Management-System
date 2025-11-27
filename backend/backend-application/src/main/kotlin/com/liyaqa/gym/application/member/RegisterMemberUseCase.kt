package com.liyaqa.gym.application.member

import com.liyaqa.gym.application.member.commands.RegisterMemberCommand
import com.liyaqa.gym.application.member.dto.MemberDTO
import com.liyaqa.gym.application.member.dto.MemberMapper
import com.liyaqa.gym.common.exception.ConflictException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.common.validation.isValidSaudiNationalId
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.MemberRegisteredEvent
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.gym.domain.valueobjects.ContactInfo
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Use case for registering a new member.
 *
 * This use case handles:
 * - Input validation (national ID, email format, etc.)
 * - Email uniqueness check
 * - Member entity creation
 * - Repository persistence
 * - Cache storage
 * - Domain event publishing
 *
 * @property memberRepository Repository for member persistence
 * @property eventPublisher Publisher for domain events
 * @property memberMapper Mapper for DTO conversion
 */
@Service
@Transactional
class RegisterMemberUseCase(
    private val memberRepository: MemberRepository,
    private val eventPublisher: EventPublisher,
    private val memberMapper: MemberMapper
) {

    private val logger = LoggerFactory.getLogger(RegisterMemberUseCase::class.java)

    /**
     * Executes the member registration use case.
     *
     * @param command The registration command containing member details
     * @return Result containing the registered member DTO or error
     */
    @CachePut(
        value = ["member"],
        key = "#result.getOrNull()?.branchId + ':' + #result.getOrNull()?.id",
        condition = "#result.isSuccess"
    )
    fun execute(command: RegisterMemberCommand): Result<MemberDTO> {
        return runCatching {
            logger.info("Registering new member with email: ${command.email} for branch: ${command.branchId}")

            // 1. Validate input
            validateCommand(command)

            // 2. Check email uniqueness
            checkEmailUniqueness(command.email)

            // 3. Create contact info value object
            val contactInfo = ContactInfo.of(command.email, command.phone)

            // 4. Create member entity
            val member = Member.create(
                tenantId = command.organizationId, // tenantId is same as organizationId
                organizationId = command.organizationId,
                branchId = command.branchId,
                name = command.name,
                nameArabic = command.nameArabic,
                contactInfo = contactInfo,
                nationalId = command.nationalId,
                gender = command.gender,
                dateOfBirth = command.dateOfBirth
            )

            // 5. Add optional fields if provided
            val updatedMember = member.copy(
                emergencyContactName = command.emergencyContactName,
                emergencyContactPhone = command.emergencyContactPhone,
                profilePhotoUrl = command.profilePhotoUrl,
                notes = command.notes
            )

            // 6. Persist to repository
            val savedMember = memberRepository.save(updatedMember)
                .getOrElse { error ->
                    logger.error("Failed to save member to repository: ${error.message}", error)
                    throw error
                }

            logger.info("Member registered successfully with ID: ${savedMember.id}")

            // 7. Publish domain event
            publishMemberRegisteredEvent(savedMember)

            // 8. Convert to DTO and return
            memberMapper.toDTO(savedMember)

        }.onFailure { error ->
            logger.error("Failed to register member: ${error.message}", error)
        }
    }

    /**
     * Validates the registration command.
     *
     * @param command The registration command to validate
     * @throws ValidationException if validation fails
     */
    private fun validateCommand(command: RegisterMemberCommand) {
        // Validate email format
        if (!command.email.matches(EMAIL_REGEX)) {
            throw ValidationException("Invalid email format: ${command.email}")
        }

        // Validate phone format
        if (!command.phone.matches(PHONE_REGEX)) {
            throw ValidationException("Invalid phone format: ${command.phone}")
        }

        // Validate Saudi national ID if provided
        command.nationalId?.let { nationalId ->
            if (!nationalId.isValidSaudiNationalId()) {
                throw ValidationException(
                    "Invalid Saudi National ID format. Must be 10 digits starting with 1 or 2 and pass checksum validation."
                )
            }
            logger.debug("Saudi National ID validated successfully: $nationalId")
        }

        // Validate date of birth (must be in the past and member must be at least 10 years old)
        command.dateOfBirth?.let { dob ->
            val now = java.time.LocalDate.now()
            if (dob.isAfter(now)) {
                throw ValidationException("Date of birth cannot be in the future")
            }

            val age = now.year - dob.year
            if (age < 10) {
                throw ValidationException("Member must be at least 10 years old")
            }

            if (age > 120) {
                throw ValidationException("Invalid date of birth: age cannot exceed 120 years")
            }
        }

        logger.debug("Command validation passed for member: ${command.name}")
    }

    /**
     * Checks if a member with the given email already exists.
     *
     * @param email The email to check
     * @throws ConflictException if email already exists
     */
    private fun checkEmailUniqueness(email: String) {
        val exists = memberRepository.existsByEmail(email.lowercase().trim())
            .getOrElse { error ->
                logger.error("Failed to check email uniqueness: ${error.message}", error)
                throw error
            }

        if (exists) {
            logger.warn("Attempted to register member with existing email: $email")
            throw ConflictException("A member with email '$email' already exists")
        }

        logger.debug("Email uniqueness check passed for: $email")
    }

    /**
     * Publishes the MemberRegisteredEvent after successful registration.
     *
     * @param member The registered member
     */
    private fun publishMemberRegisteredEvent(member: Member) {
        try {
            val event = MemberRegisteredEvent(
                memberId = member.id,
                branchId = member.branchId,
                timestamp = Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published MemberRegisteredEvent for member: ${member.id}")

        } catch (e: Exception) {
            // Log error but don't fail the registration
            // Event publishing failures should not prevent member registration
            logger.error("Failed to publish MemberRegisteredEvent for member ${member.id}: ${e.message}", e)
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}$")
    }
}
