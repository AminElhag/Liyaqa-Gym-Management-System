package com.liyaqa.gym.application.access

import com.liyaqa.gym.application.access.commands.RevokeAccessCommand
import com.liyaqa.gym.application.access.dto.BlacklistDTO
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.Blacklist
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.entities.MemberStatus
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.MemberBlacklistedEvent
import com.liyaqa.gym.domain.repositories.AccessLogRepository
import com.liyaqa.gym.domain.repositories.BlacklistRepository
import com.liyaqa.gym.domain.repositories.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use case for revoking access and blacklisting a member.
 *
 * This use case handles:
 * - Member validation
 * - Member status update (suspend)
 * - Blacklist entry creation
 * - Force checkout if currently checked in
 * - Notification to access control system (via events)
 * - Event publishing
 *
 * @property memberRepository Repository for member operations
 * @property blacklistRepository Repository for blacklist operations
 * @property accessLogRepository Repository for access log operations
 * @property eventPublisher Publisher for domain events
 */
@Service
@Transactional
class RevokeAccessUseCase(
    private val memberRepository: MemberRepository,
    private val blacklistRepository: BlacklistRepository,
    private val accessLogRepository: AccessLogRepository,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(RevokeAccessUseCase::class.java)

    /**
     * Executes the revoke access use case.
     *
     * @param command The revoke access command
     * @return Result containing blacklist information
     */
    fun execute(command: RevokeAccessCommand): Result<BlacklistDTO> {
        return runCatching {
            logger.info("Revoking access for member: ${command.memberId}, reason: ${command.reason}")

            // 1. Validate member exists
            val member = getMember(command.memberId)

            // 2. Update member status to SUSPENDED
            val suspendedMember = suspendMember(member)

            // 3. Create blacklist entry
            val blacklist = createBlacklistEntry(command)

            // 4. Force checkout if currently checked in and command requests it
            if (command.forceCheckOut) {
                forceCheckOut(command.memberId, command.branchId)
            }

            // 5. Publish MemberBlacklistedEvent (will notify access control systems)
            publishBlacklistedEvent(blacklist)

            logger.info("Access revoked for member: ${command.memberId}, blacklist ID: ${blacklist.id}")

            // 6. Return blacklist information
            BlacklistDTO(
                blacklistId = blacklist.id,
                memberId = command.memberId,
                memberName = suspendedMember.name,
                branchId = command.branchId,
                reason = command.reason,
                blacklistedAt = blacklist.blacklistedAt,
                expiresAt = command.expiresAt,
                message = "Member ${suspendedMember.name} has been blacklisted and suspended"
            )

        }.onFailure { error ->
            logger.error("Failed to revoke access: ${error.message}", error)
        }
    }

    /**
     * Gets and validates member.
     */
    private fun getMember(memberId: java.util.UUID): Member {
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
     * Suspends the member.
     */
    private fun suspendMember(member: Member): Member {
        val suspendedMember = member.suspend()

        return memberRepository.save(suspendedMember)
            .getOrElse { error ->
                logger.error("Failed to suspend member: ${error.message}", error)
                throw error
            }
    }

    /**
     * Creates a blacklist entry.
     */
    private fun createBlacklistEntry(command: RevokeAccessCommand): Blacklist {
        val blacklist = Blacklist.create(
            memberId = command.memberId,
            branchId = command.branchId,
            reason = command.reason,
            blacklistedBy = command.revokedBy,
            expiresAt = command.expiresAt,
            notes = command.notes
        )

        return blacklistRepository.save(blacklist)
            .getOrElse { error ->
                logger.error("Failed to save blacklist entry: ${error.message}", error)
                throw error
            }
    }

    /**
     * Forces checkout if member is currently checked in.
     */
    private fun forceCheckOut(memberId: java.util.UUID, branchId: java.util.UUID?) {
        try {
            if (branchId == null) {
                // System-wide blacklist, need to check all branches
                logger.info("System-wide blacklist - cannot force checkout without branch ID")
                return
            }

            val activeAccessOpt = accessLogRepository.findActiveByMemberAndBranch(memberId, branchId)
                .getOrElse { error ->
                    logger.warn("Failed to check for active access: ${error.message}")
                    return
                }

            if (activeAccessOpt.isPresent) {
                val activeAccess = activeAccessOpt.get()
                val checkedOutLog = activeAccess.checkOut()

                accessLogRepository.save(checkedOutLog)
                    .getOrElse { error ->
                        logger.error("Failed to force checkout: ${error.message}", error)
                        throw error
                    }

                logger.info("Forced checkout for member $memberId from access log ${activeAccess.id}")
            } else {
                logger.debug("Member $memberId not currently checked in, no force checkout needed")
            }
        } catch (e: Exception) {
            logger.error("Failed to force checkout: ${e.message}", e)
            // Don't fail the revocation if force checkout fails
        }
    }

    /**
     * Publishes MemberBlacklistedEvent.
     * This event will be consumed by access control system sync handlers.
     */
    private fun publishBlacklistedEvent(blacklist: Blacklist) {
        try {
            val event = MemberBlacklistedEvent(
                memberId = blacklist.memberId,
                branchId = blacklist.branchId,
                blacklistId = blacklist.id,
                reason = blacklist.reason,
                blacklistedBy = blacklist.blacklistedBy
            )
            eventPublisher.publish(event)
            logger.info("Published MemberBlacklistedEvent for member: ${blacklist.memberId}")
        } catch (e: Exception) {
            logger.error("Failed to publish MemberBlacklistedEvent: ${e.message}", e)
        }
    }
}
