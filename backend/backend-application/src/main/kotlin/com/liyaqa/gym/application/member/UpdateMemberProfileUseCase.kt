package com.liyaqa.gym.application.member

import com.liyaqa.gym.application.member.commands.UpdateMemberCommand
import com.liyaqa.gym.application.member.dto.MemberDTO
import com.liyaqa.gym.application.member.dto.MemberMapper
import com.liyaqa.gym.common.exception.ConflictException
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.common.validation.isValidSaudiNationalId
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.MemberProfileUpdatedEvent
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.gym.domain.valueobjects.ContactInfo
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Use case for updating a member's profile.
 *
 * This use case handles:
 * - Member existence validation
 * - Input validation
 * - Email uniqueness check (if email is being updated)
 * - Member entity update
 * - Repository persistence
 * - Cache invalidation
 * - Domain event publishing
 *
 * @property memberRepository Repository for member persistence
 * @property eventPublisher Publisher for domain events
 * @property memberMapper Mapper for DTO conversion
 */
@Service
@Transactional
class UpdateMemberProfileUseCase(
    private val memberRepository: MemberRepository,
    private val eventPublisher: EventPublisher,
    private val memberMapper: MemberMapper
) {

    private val logger = LoggerFactory.getLogger(UpdateMemberProfileUseCase::class.java)

    /**
     * Executes the member profile update use case.
     *
     * @param command The update command containing member ID and fields to update
     * @return Result containing the updated member DTO or error
     */
    @CacheEvict(
        value = ["member"],
        key = "#result.getOrNull()?.branchId + ':' + #command.memberId",
        condition = "#result.isSuccess"
    )
    fun execute(command: UpdateMemberCommand): Result<MemberDTO> {
        return runCatching {
            logger.info("Updating member profile for member ID: ${command.memberId}")

            // 1. Fetch existing member
            val existingMember = fetchMember(command.memberId)

            // 2. Validate command
            validateCommand(command, existingMember)

            // 3. Build list of updated fields for event
            val updatedFields = mutableListOf<String>()

            // 4. Apply updates to member entity
            var updatedMember = existingMember

            command.name?.let {
                updatedMember = updatedMember.copy(name = it)
                updatedFields.add("name")
            }

            command.nameArabic?.let {
                updatedMember = updatedMember.copy(nameArabic = it)
                updatedFields.add("nameArabic")
            }

            // Update contact info if email or phone changed
            if (command.email != null || command.phone != null) {
                val newEmail = command.email ?: existingMember.contactInfo.email
                val newPhone = command.phone ?: existingMember.contactInfo.phone
                val newContactInfo = ContactInfo.of(newEmail, newPhone)
                updatedMember = updatedMember.updateContactInfo(newContactInfo)

                if (command.email != null) updatedFields.add("email")
                if (command.phone != null) updatedFields.add("phone")
            }

            command.nationalId?.let {
                updatedMember = updatedMember.copy(nationalId = it)
                updatedFields.add("nationalId")
            }

            command.gender?.let {
                updatedMember = updatedMember.copy(gender = it)
                updatedFields.add("gender")
            }

            command.dateOfBirth?.let {
                updatedMember = updatedMember.copy(dateOfBirth = it)
                updatedFields.add("dateOfBirth")
            }

            command.emergencyContactName?.let {
                updatedMember = updatedMember.copy(emergencyContactName = it)
                updatedFields.add("emergencyContactName")
            }

            command.emergencyContactPhone?.let {
                updatedMember = updatedMember.copy(emergencyContactPhone = it)
                updatedFields.add("emergencyContactPhone")
            }

            command.profilePhotoUrl?.let {
                updatedMember = updatedMember.copy(profilePhotoUrl = it)
                updatedFields.add("profilePhotoUrl")
            }

            command.notes?.let {
                updatedMember = updatedMember.copy(notes = it)
                updatedFields.add("notes")
            }

            // 5. Update timestamp
            updatedMember = updatedMember.copy(updatedAt = Instant.now())

            // 6. Persist to repository
            val savedMember = memberRepository.save(updatedMember)
                .getOrElse { error ->
                    logger.error("Failed to save updated member to repository: ${error.message}", error)
                    throw error
                }

            logger.info("Member profile updated successfully for member ID: ${command.memberId}")

            // 7. Publish domain event
            publishMemberProfileUpdatedEvent(savedMember, updatedFields)

            // 8. Convert to DTO and return
            memberMapper.toDTO(savedMember)

        }.onFailure { error ->
            logger.error("Failed to update member profile: ${error.message}", error)
        }
    }

    /**
     * Fetches the member by ID.
     *
     * @param memberId The member ID
     * @return The member entity
     * @throws ResourceNotFoundException if member not found
     */
    private fun fetchMember(memberId: java.util.UUID): Member {
        return memberRepository.findById(memberId)
            .getOrElse { error ->
                logger.error("Failed to fetch member: ${error.message}", error)
                throw error
            }
            .orElseThrow {
                logger.warn("Member not found with ID: $memberId")
                ResourceNotFoundException("Member not found with ID: $memberId")
            }
    }

    /**
     * Validates the update command.
     *
     * @param command The update command to validate
     * @param existingMember The existing member entity
     * @throws ValidationException if validation fails
     */
    private fun validateCommand(command: UpdateMemberCommand, existingMember: Member) {
        // Validate email format if provided
        command.email?.let { email ->
            if (!email.matches(EMAIL_REGEX)) {
                throw ValidationException("Invalid email format: $email")
            }

            // Check email uniqueness (excluding current member)
            val emailExists = memberRepository.existsByEmailExcludingMember(
                email.lowercase().trim(),
                command.memberId
            ).getOrElse { error ->
                logger.error("Failed to check email uniqueness: ${error.message}", error)
                throw error
            }

            if (emailExists) {
                logger.warn("Attempted to update member with existing email: $email")
                throw ConflictException("A member with email '$email' already exists")
            }
        }

        // Validate phone format if provided
        command.phone?.let { phone ->
            if (!phone.matches(PHONE_REGEX)) {
                throw ValidationException("Invalid phone format: $phone")
            }
        }

        // Validate Saudi national ID if provided
        command.nationalId?.let { nationalId ->
            if (!nationalId.isValidSaudiNationalId()) {
                throw ValidationException(
                    "Invalid Saudi National ID format. Must be 10 digits starting with 1 or 2 and pass checksum validation."
                )
            }
        }

        // Validate date of birth if provided
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

        logger.debug("Command validation passed for member: ${command.memberId}")
    }

    /**
     * Publishes the MemberProfileUpdatedEvent after successful update.
     *
     * @param member The updated member
     * @param updatedFields List of fields that were updated
     */
    private fun publishMemberProfileUpdatedEvent(member: Member, updatedFields: List<String>) {
        try {
            val event = MemberProfileUpdatedEvent(
                memberId = member.id,
                branchId = member.branchId,
                updatedFields = updatedFields,
                updatedBy = null, // TODO: Get from security context
                timestamp = Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published MemberProfileUpdatedEvent for member: ${member.id}")

        } catch (e: Exception) {
            // Log error but don't fail the update
            logger.error("Failed to publish MemberProfileUpdatedEvent for member ${member.id}: ${e.message}", e)
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}$")
    }
}
