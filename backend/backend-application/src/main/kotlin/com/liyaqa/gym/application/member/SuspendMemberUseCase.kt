package com.liyaqa.gym.application.member

import com.liyaqa.gym.application.member.commands.SuspendMemberCommand
import com.liyaqa.gym.application.member.dto.MemberDTO
import com.liyaqa.gym.application.member.dto.MemberMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.MemberStatus
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.MemberSuspendedEvent
import com.liyaqa.gym.domain.repositories.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Use case for suspending a member.
 *
 * This use case handles:
 * - Member existence validation
 * - Status validation (cannot suspend already suspended members)
 * - Member suspension
 * - Repository persistence
 * - Cache invalidation
 * - Domain event publishing
 * - Optional notification trigger
 *
 * @property memberRepository Repository for member persistence
 * @property eventPublisher Publisher for domain events
 * @property memberMapper Mapper for DTO conversion
 */
@Service
@Transactional
class SuspendMemberUseCase(
    private val memberRepository: MemberRepository,
    private val eventPublisher: EventPublisher,
    private val memberMapper: MemberMapper
) {

    private val logger = LoggerFactory.getLogger(SuspendMemberUseCase::class.java)

    /**
     * Executes the member suspension use case.
     *
     * @param command The suspension command containing member ID and reason
     * @return Result containing the suspended member DTO or error
     */
    @CacheEvict(
        value = ["member"],
        key = "#result.getOrNull()?.branchId + ':' + #command.memberId",
        condition = "#result.isSuccess"
    )
    fun execute(command: SuspendMemberCommand): Result<MemberDTO> {
        return runCatching {
            logger.info("Suspending member with ID: ${command.memberId}, reason: ${command.reason}")

            // 1. Fetch existing member
            val existingMember = memberRepository.findById(command.memberId)
                .getOrElse { error ->
                    logger.error("Failed to fetch member: ${error.message}", error)
                    throw error
                }
                .orElseThrow {
                    logger.warn("Member not found with ID: ${command.memberId}")
                    ResourceNotFoundException("Member not found with ID: ${command.memberId}")
                }

            // 2. Validate member can be suspended
            validateSuspension(existingMember.status)

            // 3. Suspend member
            val suspendedMember = existingMember.suspend()

            // 4. Persist to repository
            val savedMember = memberRepository.save(suspendedMember)
                .getOrElse { error ->
                    logger.error("Failed to save suspended member: ${error.message}", error)
                    throw error
                }

            logger.info("Member suspended successfully with ID: ${command.memberId}")

            // 5. Publish domain event
            publishMemberSuspendedEvent(savedMember, command)

            // 6. TODO: Send notification to member if requested
            if (command.notifyMember) {
                logger.debug("Notification requested for suspended member: ${command.memberId}")
                // This would typically trigger a notification service
                // For now, we just log it
            }

            // 7. Convert to DTO and return
            memberMapper.toDTO(savedMember)

        }.onFailure { error ->
            logger.error("Failed to suspend member: ${error.message}", error)
        }
    }

    /**
     * Validates that the member can be suspended.
     *
     * @param currentStatus The current status of the member
     * @throws ValidationException if member is already suspended or inactive
     */
    private fun validateSuspension(currentStatus: MemberStatus) {
        when (currentStatus) {
            MemberStatus.SUSPENDED -> {
                throw ValidationException("Member is already suspended")
            }
            MemberStatus.INACTIVE -> {
                throw ValidationException("Cannot suspend an inactive member")
            }
            else -> {
                logger.debug("Member status validation passed: $currentStatus")
            }
        }
    }

    /**
     * Publishes the MemberSuspendedEvent after successful suspension.
     *
     * @param member The suspended member
     * @param command The suspension command
     */
    private fun publishMemberSuspendedEvent(
        member: com.liyaqa.gym.domain.entities.Member,
        command: SuspendMemberCommand
    ) {
        try {
            val event = MemberSuspendedEvent(
                memberId = member.id,
                branchId = member.branchId,
                reason = command.reason,
                suspendedBy = command.suspendedBy,
                timestamp = Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published MemberSuspendedEvent for member: ${member.id}")

        } catch (e: Exception) {
            // Log error but don't fail the suspension
            logger.error("Failed to publish MemberSuspendedEvent for member ${member.id}: ${e.message}", e)
        }
    }

    /**
     * Batch suspend multiple members with the same reason.
     * Useful for bulk operations like branch closure.
     *
     * @param memberIds List of member IDs to suspend
     * @param reason Reason for suspension
     * @param suspendedBy User ID performing the suspension
     * @return Result containing a list of suspended member DTOs
     */
    fun suspendMultiple(
        memberIds: List<java.util.UUID>,
        reason: String,
        suspendedBy: java.util.UUID
    ): Result<List<MemberDTO>> {
        return runCatching {
            logger.info("Suspending ${memberIds.size} members")

            if (memberIds.isEmpty()) {
                return@runCatching emptyList()
            }

            val suspendedMembers = memberIds.mapNotNull { memberId ->
                val command = SuspendMemberCommand(
                    memberId = memberId,
                    reason = reason,
                    suspendedBy = suspendedBy,
                    notifyMember = false // Don't notify during bulk operations
                )

                execute(command).getOrNull()
            }

            logger.info("Successfully suspended ${suspendedMembers.size} out of ${memberIds.size} members")
            suspendedMembers

        }.onFailure { error ->
            logger.error("Failed to suspend multiple members: ${error.message}", error)
        }
    }
}
