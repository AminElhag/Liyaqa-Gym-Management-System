package com.liyaqa.gym.application.access

import com.liyaqa.gym.application.access.commands.GenerateGuestAccessCommand
import com.liyaqa.gym.application.access.dto.GuestAccessDTO
import com.liyaqa.gym.application.access.dto.GuestAccessMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.GuestAccess
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.entities.MemberStatus
import com.liyaqa.gym.domain.entities.SubscriptionStatus
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.GuestAccessGeneratedEvent
import com.liyaqa.gym.domain.repositories.BranchRepository
import com.liyaqa.gym.domain.repositories.GuestAccessRepository
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.gym.domain.repositories.SubscriptionRepository
import com.liyaqa.gym.domain.services.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use case for generating temporary guest access codes.
 *
 * This use case handles:
 * - Host member validation (exists, active, has valid subscription)
 * - Guest limit validation (max guests per member)
 * - Temporary QR code generation (valid for specified hours, default 24)
 * - Guest count tracking
 * - Event publishing
 * - Notification sending (QR code to host member)
 *
 * @property memberRepository Repository for member operations
 * @property subscriptionRepository Repository for subscription operations
 * @property branchRepository Repository for branch operations
 * @property guestAccessRepository Repository for guest access operations
 * @property eventPublisher Publisher for domain events
 * @property notificationService Service for sending notifications
 */
@Service
@Transactional
class GenerateTemporaryAccessUseCase(
    private val memberRepository: MemberRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val branchRepository: BranchRepository,
    private val guestAccessRepository: GuestAccessRepository,
    private val eventPublisher: EventPublisher,
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(GenerateTemporaryAccessUseCase::class.java)

    companion object {
        private const val MAX_ACTIVE_GUEST_ACCESSES = 3
    }

    /**
     * Executes the generate guest access use case.
     *
     * @param command The generate guest access command
     * @return Result containing guest access DTO with QR code
     */
    fun execute(command: GenerateGuestAccessCommand): Result<GuestAccessDTO> {
        return runCatching {
            logger.info("Generating guest access for host member: ${command.hostMemberId}, guest: ${command.guestName}")

            // 1. Validate host member exists and is active
            val hostMember = validateHostMember(command.hostMemberId)

            // 2. Validate host member has active subscription
            validateHostSubscription(command.hostMemberId)

            // 3. Validate branch exists and is active
            validateBranch(command.branchId)

            // 4. Check guest limit (max active guest accesses)
            validateGuestLimit(command.hostMemberId)

            // 5. Generate guest access with QR code
            val guestAccess = createGuestAccess(command)

            // 6. Publish event
            publishGuestAccessGeneratedEvent(guestAccess)

            // 7. Send QR code to host member
            sendGuestAccessNotification(hostMember, guestAccess)

            logger.info("Guest access generated successfully: ${guestAccess.id}, QR: ${guestAccess.qrCode}")

            // 8. Return guest access DTO
            GuestAccessMapper.toDTO(guestAccess)

        }.onFailure { error ->
            logger.error("Failed to generate guest access: ${error.message}", error)
        }
    }

    /**
     * Validates host member.
     */
    private fun validateHostMember(memberId: java.util.UUID): Member {
        val memberOpt = memberRepository.findById(memberId)
            .getOrElse { error ->
                logger.error("Failed to query member repository: ${error.message}", error)
                throw error
            }

        if (!memberOpt.isPresent) {
            logger.warn("Host member not found: $memberId")
            throw ResourceNotFoundException("Member with ID $memberId not found")
        }

        val member = memberOpt.get()

        if (member.status != MemberStatus.ACTIVE) {
            logger.warn("Host member is not active: $memberId (status: ${member.status})")
            throw ValidationException("Cannot generate guest access. Member status is ${member.status}.")
        }

        return member
    }

    /**
     * Validates host member has active subscription.
     */
    private fun validateHostSubscription(memberId: java.util.UUID) {
        val subscriptions = subscriptionRepository.findByMember(memberId)
            .getOrElse { error ->
                logger.error("Failed to query subscriptions: ${error.message}", error)
                throw error
            }

        val activeSubscription = subscriptions.firstOrNull { it.status == SubscriptionStatus.ACTIVE }

        if (activeSubscription == null) {
            logger.warn("Host member $memberId has no active subscription")
            throw ValidationException("Cannot generate guest access. No active subscription found.")
        }

        if (activeSubscription.isExpired()) {
            logger.warn("Host member $memberId subscription has expired")
            throw ValidationException("Cannot generate guest access. Subscription has expired.")
        }
    }

    /**
     * Validates branch exists and is active.
     */
    private fun validateBranch(branchId: java.util.UUID) {
        val branchOpt = branchRepository.findById(branchId)
            .getOrElse { error ->
                logger.error("Failed to query branch repository: ${error.message}", error)
                throw error
            }

        if (!branchOpt.isPresent) {
            logger.warn("Branch not found: $branchId")
            throw ResourceNotFoundException("Branch with ID $branchId not found")
        }

        val branch = branchOpt.get()

        if (!branch.isActive) {
            logger.warn("Branch is not active: $branchId")
            throw ValidationException("Branch is currently inactive")
        }
    }

    /**
     * Validates guest limit.
     */
    private fun validateGuestLimit(hostMemberId: java.util.UUID) {
        val activeGuestCount = guestAccessRepository.countActiveByHostMember(hostMemberId)
            .getOrElse { error ->
                logger.error("Failed to count active guest accesses: ${error.message}", error)
                throw error
            }

        if (activeGuestCount >= MAX_ACTIVE_GUEST_ACCESSES) {
            logger.warn("Host member $hostMemberId has reached guest limit: $activeGuestCount/$MAX_ACTIVE_GUEST_ACCESSES")
            throw ValidationException(
                "Guest limit reached. You have $activeGuestCount active guest access codes. " +
                        "Maximum allowed: $MAX_ACTIVE_GUEST_ACCESSES"
            )
        }
    }

    /**
     * Creates guest access with QR code.
     */
    private fun createGuestAccess(command: GenerateGuestAccessCommand): GuestAccess {
        val guestAccess = GuestAccess.create(
            hostMemberId = command.hostMemberId,
            branchId = command.branchId,
            guestName = command.guestName,
            guestPhone = command.guestPhone,
            validityHours = command.validityHours
        )

        return guestAccessRepository.save(guestAccess)
            .getOrElse { error ->
                logger.error("Failed to save guest access: ${error.message}", error)
                throw error
            }
    }

    /**
     * Publishes GuestAccessGeneratedEvent.
     */
    private fun publishGuestAccessGeneratedEvent(guestAccess: GuestAccess) {
        try {
            val event = GuestAccessGeneratedEvent(
                guestAccessId = guestAccess.id,
                hostMemberId = guestAccess.hostMemberId,
                branchId = guestAccess.branchId,
                guestName = guestAccess.guestName,
                validUntil = guestAccess.validUntil
            )
            eventPublisher.publish(event)
            logger.info("Published GuestAccessGeneratedEvent for guest access: ${guestAccess.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish GuestAccessGeneratedEvent: ${e.message}", e)
        }
    }

    /**
     * Sends guest access code notification to host member.
     */
    private fun sendGuestAccessNotification(hostMember: Member, guestAccess: GuestAccess) {
        try {
            notificationService.sendGuestAccessCode(
                hostMemberId = hostMember.id,
                guestAccessId = guestAccess.id,
                guestName = guestAccess.guestName,
                qrCode = guestAccess.qrCode
            ).getOrElse { error ->
                logger.warn("Failed to send guest access notification: ${error.message}")
            }
        } catch (e: Exception) {
            logger.error("Failed to send guest access notification: ${e.message}", e)
        }
    }
}
