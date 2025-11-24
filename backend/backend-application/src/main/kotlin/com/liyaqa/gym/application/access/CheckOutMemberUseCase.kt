package com.liyaqa.gym.application.access

import com.liyaqa.gym.application.access.commands.CheckOutCommand
import com.liyaqa.gym.application.access.dto.CheckOutConfirmation
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.AccessLog
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.MemberCheckedOutEvent
import com.liyaqa.gym.domain.repositories.AccessLogRepository
import com.liyaqa.gym.domain.repositories.BranchCapacityRepository
import com.liyaqa.gym.domain.repositories.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.UUID

/**
 * Use case for checking out a member from a gym branch.
 *
 * This use case handles:
 * - Finding active access log
 * - Updating access log with checkout time
 * - Calculating visit duration
 * - Updating branch capacity
 * - Publishing domain events
 *
 * @property accessLogRepository Repository for access log operations
 * @property memberRepository Repository for member operations
 * @property branchCapacityRepository Repository for capacity management
 * @property eventPublisher Publisher for domain events
 */
@Service
@Transactional
class CheckOutMemberUseCase(
    private val accessLogRepository: AccessLogRepository,
    private val memberRepository: MemberRepository,
    private val branchCapacityRepository: BranchCapacityRepository,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(CheckOutMemberUseCase::class.java)

    /**
     * Executes the check-out use case.
     *
     * @param command The check-out command
     * @return Result containing check-out confirmation
     */
    fun execute(command: CheckOutCommand): Result<CheckOutConfirmation> {
        return runCatching {
            logger.info("Processing check-out for member: ${command.memberId}, accessLogId: ${command.accessLogId}, branch: ${command.branchId}")

            // 1. Find the active access log
            val accessLog = findActiveAccessLog(command)

            // 2. Get member information
            val member = getMember(accessLog.memberId)

            // 3. Update access log with checkout time
            val updatedAccessLog = checkOutAccessLog(accessLog)

            // 4. Calculate duration
            val duration = updatedAccessLog.getDuration() ?: Duration.ZERO

            // 5. Update branch capacity (decrement)
            updateCapacity(accessLog.branchId, member)

            // 6. Publish MemberCheckedOutEvent
            publishCheckedOutEvent(accessLog.memberId, accessLog.branchId, duration)

            logger.info("Check-out successful for member: ${accessLog.memberId}, duration: ${duration.toMinutes()} minutes")

            // 7. Return confirmation
            val checkOutTime = updatedAccessLog.checkOutTime
                ?: throw IllegalStateException("Check-out time should be set after calling checkOut()")

            CheckOutConfirmation(
                accessLogId = updatedAccessLog.id,
                memberId = accessLog.memberId,
                branchId = accessLog.branchId,
                checkInTime = accessLog.checkInTime,
                checkOutTime = checkOutTime,
                duration = duration,
                message = "Thank you for visiting! You stayed for ${duration.toMinutes()} minutes."
            )

        }.onFailure { error ->
            logger.error("Failed to check out member: ${error.message}", error)
        }
    }

    /**
     * Finds the active access log based on command.
     */
    private fun findActiveAccessLog(command: CheckOutCommand): AccessLog {
        if (command.accessLogId != null) {
            // Find by access log ID
            val accessLogOpt = accessLogRepository.findById(command.accessLogId)
                .getOrElse { error ->
                    logger.error("Failed to query access log repository: ${error.message}", error)
                    throw error
                }

            if (!accessLogOpt.isPresent) {
                logger.warn("Access log not found: ${command.accessLogId}")
                throw ResourceNotFoundException("Access log with ID ${command.accessLogId} not found")
            }

            val accessLog = accessLogOpt.get()

            if (accessLog.isCheckedOut()) {
                logger.warn("Access log ${command.accessLogId} is already checked out")
                throw ValidationException("Already checked out")
            }

            return accessLog

        } else {
            // Find by member ID and branch ID
            val memberId = command.memberId
                ?: throw ValidationException("memberId is required when accessLogId is not provided")
            val branchId = command.branchId
                ?: throw ValidationException("branchId is required when accessLogId is not provided")

            val activeAccessOpt = accessLogRepository.findActiveByMemberAndBranch(
                memberId,
                branchId
            ).getOrElse { error ->
                logger.error("Failed to query active access: ${error.message}", error)
                throw error
            }

            if (!activeAccessOpt.isPresent) {
                logger.warn("No active check-in found for member ${command.memberId} at branch ${command.branchId}")
                throw ValidationException("No active check-in found. Member may not be checked in.")
            }

            return activeAccessOpt.get()
        }
    }

    /**
     * Gets member information.
     */
    private fun getMember(memberId: UUID): Member {
        val memberOpt = memberRepository.findById(memberId)
            .getOrElse { error ->
                logger.error("Failed to query member repository: ${error.message}", error)
                throw error
            }

        if (!memberOpt.isPresent) {
            logger.warn("Member not found: $memberId")
            throw ResourceNotFoundException("Member with ID $memberId not found")
        }

        return memberOpt.get()
    }

    /**
     * Updates access log with checkout time.
     */
    private fun checkOutAccessLog(accessLog: AccessLog): AccessLog {
        val checkedOutLog = accessLog.checkOut()

        return accessLogRepository.save(checkedOutLog)
            .getOrElse { error ->
                logger.error("Failed to save access log: ${error.message}", error)
                throw error
            }
    }

    /**
     * Updates branch capacity (decrement).
     */
    private fun updateCapacity(branchId: UUID, member: Member) {
        try {
            branchCapacityRepository.decrementOccupancy(
                branchId,
                isMale = member.gender == com.liyaqa.gym.domain.entities.Gender.MALE
            )
            logger.info("Decremented capacity for branch: $branchId")
        } catch (e: Exception) {
            logger.error("Failed to update capacity: ${e.message}", e)
            // Don't fail check-out if capacity update fails
        }
    }

    /**
     * Publishes MemberCheckedOutEvent.
     */
    private fun publishCheckedOutEvent(memberId: UUID, branchId: UUID, duration: Duration) {
        try {
            val event = MemberCheckedOutEvent(
                memberId = memberId,
                branchId = branchId,
                duration = duration,
                timestamp = java.time.Instant.now()
            )
            eventPublisher.publish(event)
            logger.info("Published MemberCheckedOutEvent for member: $memberId")
        } catch (e: Exception) {
            logger.error("Failed to publish MemberCheckedOutEvent: ${e.message}", e)
        }
    }
}
