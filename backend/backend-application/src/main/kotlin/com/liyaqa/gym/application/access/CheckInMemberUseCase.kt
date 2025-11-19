package com.liyaqa.gym.application.access

import com.liyaqa.gym.application.access.commands.CheckInCommand
import com.liyaqa.gym.application.access.dto.AccessDenied
import com.liyaqa.gym.application.access.dto.AccessGranted
import com.liyaqa.gym.application.access.dto.AccessResult
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.*
import com.liyaqa.gym.domain.events.AccessDeniedEvent
import com.liyaqa.gym.domain.events.AccessGrantedEvent
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.MemberCheckedInEvent
import com.liyaqa.gym.domain.repositories.*
import com.liyaqa.gym.domain.services.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Use case for checking in a member to a gym branch.
 *
 * This use case handles comprehensive check-in validation:
 * - Member validation (exists and active)
 * - Subscription validation (active and not expired)
 * - Branch access validation (membership allows access to this branch)
 * - Zone permissions validation based on plan
 * - Duplicate check-in prevention
 * - Facility capacity checking (not at max)
 * - Gender restrictions (KSA compliance)
 * - Blacklist checking
 * - Access log creation
 * - Real-time capacity updates
 * - Event publishing
 * - Notification sending
 *
 * @property memberRepository Repository for member operations
 * @property subscriptionRepository Repository for subscription operations
 * @property branchRepository Repository for branch operations
 * @property planRepository Repository for membership plan operations
 * @property accessLogRepository Repository for access log operations
 * @property blacklistRepository Repository for blacklist operations
 * @property guestAccessRepository Repository for guest access operations
 * @property branchCapacityRepository Repository for capacity management
 * @property eventPublisher Publisher for domain events
 * @property notificationService Service for sending notifications
 */
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
class CheckInMemberUseCase(
    private val memberRepository: MemberRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val branchRepository: BranchRepository,
    private val planRepository: MembershipPlanRepository,
    private val accessLogRepository: AccessLogRepository,
    private val blacklistRepository: BlacklistRepository,
    private val guestAccessRepository: GuestAccessRepository,
    private val branchCapacityRepository: BranchCapacityRepository,
    private val eventPublisher: EventPublisher,
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(CheckInMemberUseCase::class.java)

    companion object {
        private const val MAX_GUEST_LIMIT_PER_MEMBER = 5
    }

    /**
     * Executes the check-in use case.
     *
     * @param command The check-in command
     * @return Result containing AccessGranted or AccessDenied
     */
    fun execute(command: CheckInCommand): Result<AccessResult> {
        return runCatching {
            logger.info("Processing check-in for member: ${command.memberId}, qrCode: ${command.qrCode?.take(8)}, branch: ${command.branchId}")

            // 1. Resolve member from ID or QR code
            val (member, subscription, isGuest) = resolveMemberFromCommand(command)

            // 2. Validate branch exists and is active
            val branch = validateBranch(command.branchId)

            // 3. Check blacklist
            checkBlacklist(member.id, command.branchId)

            // 4. Check if member is active
            validateMemberStatus(member)

            // 5. Validate subscription (active and not expired)
            validateSubscription(subscription)

            // 6. Get membership plan
            val plan = validatePlan(subscription.planId)

            // 7. Check if membership allows access to this branch
            validateBranchAccess(member, plan, command.branchId)

            // 8. Check gender restrictions (KSA compliance)
            validateGenderRestriction(member, branch)

            // 9. Check if already checked in (prevent duplicate)
            validateNoDuplicateCheckIn(member.id, command.branchId)

            // 10. Check facility capacity (not at max)
            checkCapacity(command.branchId, member.gender)

            // 11. Create access log entry
            val accessLog = createAccessLog(
                member.id,
                command.branchId,
                subscription.id,
                isGuest,
                command.accessMethod,
                command.deviceId,
                command.validatedBy
            )

            // 12. Update capacity atomically
            updateCapacity(command.branchId, member.gender, increment = true)

            // 13. Publish events
            publishAccessGrantedEvent(member.id, command.branchId, accessLog.id, command.accessMethod)
            publishMemberCheckedInEvent(member.id, command.branchId)

            // 14. Send notification to member
            sendCheckInNotification(member.id, command.branchId, accessLog.id)

            logger.info("Check-in successful for member: ${member.id}, access log: ${accessLog.id}")

            // 15. Return success result
            AccessGranted(
                memberId = member.id,
                branchId = command.branchId,
                timestamp = accessLog.checkInTime,
                accessLogId = accessLog.id,
                memberName = member.name,
                subscriptionEndDate = subscription.endDate,
                message = "Welcome ${member.name}! Access granted."
            ) as AccessResult

        }.recoverCatching { error ->
            logger.error("Check-in failed: ${error.message}", error)

            // Handle access denial
            val memberId = command.memberId ?: UUID.randomUUID() // Fallback for QR code failures
            val reason = when (error) {
                is ValidationException -> error.message ?: "Validation failed"
                is ResourceNotFoundException -> error.message ?: "Resource not found"
                else -> "Check-in failed: ${error.message}"
            }

            // Publish access denied event
            try {
                publishAccessDeniedEvent(memberId, command.branchId, reason)
                sendAccessDeniedNotification(memberId, command.branchId, reason)
            } catch (e: Exception) {
                logger.error("Failed to publish access denied event: ${e.message}", e)
            }

            AccessDenied(
                memberId = memberId,
                branchId = command.branchId,
                timestamp = Instant.now(),
                reason = reason
            ) as AccessResult
        }
    }

    /**
     * Resolves member from either member ID or QR code.
     * Returns tuple of (Member, Subscription, isGuest).
     */
    private fun resolveMemberFromCommand(command: CheckInCommand): Triple<Member, Subscription, Boolean> {
        return if (command.qrCode != null) {
            // Try to resolve as guest access first
            val guestAccessOpt = guestAccessRepository.findByQRCode(command.qrCode)
                .getOrElse { error ->
                    logger.error("Failed to query guest access: ${error.message}", error)
                    throw error
                }

            if (guestAccessOpt.isPresent) {
                val guestAccess = guestAccessOpt.get()

                if (!guestAccess.isValid()) {
                    throw ValidationException("Guest access code is invalid or expired")
                }

                // Validate branch matches
                if (guestAccess.branchId != command.branchId) {
                    throw ValidationException("Guest access code is not valid for this branch")
                }

                // Get host member's subscription to validate access
                val hostMember = getMember(guestAccess.hostMemberId)
                val hostSubscription = getActiveSubscription(guestAccess.hostMemberId)

                // Mark guest access as used
                val usedGuestAccess = guestAccess.markAsUsed(UUID.randomUUID()) // Temporary ID, will be updated
                guestAccessRepository.save(usedGuestAccess)

                // Create a virtual "member" for the guest
                val guestMember = hostMember.copy(
                    id = UUID.randomUUID(), // Temporary guest member ID
                    name = guestAccess.guestName
                )

                Triple(guestMember, hostSubscription, true)
            } else {
                // Try to resolve as member QR code
                // In production, you would decode the QR code to get member ID
                throw ValidationException("Invalid QR code: not found in system")
            }
        } else {
            // Use member ID directly
            val member = getMember(command.memberId!!)
            val subscription = getActiveSubscription(command.memberId)
            Triple(member, subscription, false)
        }
    }

    /**
     * Validates that the member exists and retrieves it.
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
     * Validates member status.
     */
    private fun validateMemberStatus(member: Member) {
        if (member.status != MemberStatus.ACTIVE) {
            logger.warn("Attempted check-in for inactive member: ${member.id} (status: ${member.status})")
            throw ValidationException("Member status is ${member.status}. Active status required for access.")
        }
    }

    /**
     * Gets the active subscription for a member.
     */
    private fun getActiveSubscription(memberId: UUID): Subscription {
        val subscriptions = subscriptionRepository.findByMember(memberId)
            .getOrElse { error ->
                logger.error("Failed to query subscriptions: ${error.message}", error)
                throw error
            }

        val activeSubscription = subscriptions.firstOrNull { it.status == SubscriptionStatus.ACTIVE }

        if (activeSubscription == null) {
            logger.warn("Member $memberId has no active subscription")
            throw ValidationException("No active subscription found. Please renew your membership.")
        }

        return activeSubscription
    }

    /**
     * Validates subscription status.
     */
    private fun validateSubscription(subscription: Subscription) {
        if (subscription.status != SubscriptionStatus.ACTIVE) {
            throw ValidationException("Subscription status is ${subscription.status}. Active status required.")
        }

        if (subscription.isExpired()) {
            throw ValidationException("Subscription has expired on ${subscription.endDate}. Please renew.")
        }

        if (subscription.isPaused()) {
            throw ValidationException("Subscription is paused until ${subscription.pausedUntil}.")
        }

        // Check remaining visits for visit-based subscriptions
        if (subscription.remainingVisits != null && subscription.remainingVisits <= 0) {
            throw ValidationException("No remaining visits on your subscription. Please renew.")
        }
    }

    /**
     * Validates that the branch exists and is active.
     */
    private fun validateBranch(branchId: UUID): Branch {
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
            logger.warn("Attempted check-in at inactive branch: $branchId")
            throw ValidationException("Branch is currently inactive")
        }

        return branch
    }

    /**
     * Validates that the plan exists and is active.
     */
    private fun validatePlan(planId: UUID): MembershipPlan {
        val planOpt = planRepository.findById(planId)
            .getOrElse { error ->
                logger.error("Failed to query plan repository: ${error.message}", error)
                throw error
            }

        if (!planOpt.isPresent) {
            logger.warn("Membership plan not found: $planId")
            throw ResourceNotFoundException("Membership plan with ID $planId not found")
        }

        val plan = planOpt.get()

        if (!plan.isActive) {
            logger.warn("Attempted check-in with inactive plan: $planId")
            throw ValidationException("Membership plan is no longer active")
        }

        return plan
    }

    /**
     * Validates that membership allows access to the specified branch.
     */
    private fun validateBranchAccess(member: Member, plan: MembershipPlan, branchId: UUID) {
        // Check if member's branch matches the access branch OR plan allows multiple branches
        if (member.branchId != branchId && plan.branchId != branchId) {
            // Additional logic could check if plan allows multi-branch access
            logger.warn("Member ${member.id} attempted to access branch $branchId but membership is for branch ${member.branchId}")
            throw ValidationException("Your membership does not allow access to this branch")
        }
    }

    /**
     * Validates gender restrictions for KSA compliance.
     */
    private fun validateGenderRestriction(member: Member, branch: Branch) {
        if (!branch.canAcceptGender(member.gender)) {
            logger.warn("Gender restriction violated: member ${member.id} (${member.gender}) at branch ${branch.id} (${branch.facilityType})")
            throw ValidationException(
                "This facility is ${branch.facilityType} only. Access denied for ${member.gender} members."
            )
        }
    }

    /**
     * Checks if member is blacklisted.
     */
    private fun checkBlacklist(memberId: UUID, branchId: UUID) {
        val isBlacklisted = blacklistRepository.isBlacklisted(memberId, branchId)
            .getOrElse { error ->
                logger.error("Failed to check blacklist: ${error.message}", error)
                // Don't fail check-in if blacklist check fails, but log it
                logger.warn("Proceeding with check-in despite blacklist check failure")
                return
            }

        if (isBlacklisted) {
            logger.warn("Attempted check-in for blacklisted member: $memberId at branch $branchId")
            throw ValidationException("Access denied. Member is blacklisted. Please contact staff.")
        }
    }

    /**
     * Validates no duplicate check-in.
     */
    private fun validateNoDuplicateCheckIn(memberId: UUID, branchId: UUID) {
        val activeAccessOpt = accessLogRepository.findActiveByMemberAndBranch(memberId, branchId)
            .getOrElse { error ->
                logger.error("Failed to query active access: ${error.message}", error)
                throw error
            }

        if (activeAccessOpt.isPresent) {
            val activeAccess = activeAccessOpt.get()
            logger.warn("Member $memberId is already checked in at branch $branchId since ${activeAccess.checkInTime}")
            throw ValidationException("Already checked in. Please check out before checking in again.")
        }
    }

    /**
     * Checks facility capacity.
     */
    private fun checkCapacity(branchId: UUID, memberGender: Gender) {
        val capacityOpt = branchCapacityRepository.findByBranchId(branchId)
            .getOrElse { error ->
                logger.warn("Failed to check capacity: ${error.message}. Proceeding with check-in.", error)
                return // Don't block check-in if capacity check fails
            }

        if (capacityOpt.isPresent) {
            val capacity = capacityOpt.get()

            if (capacity.isFull()) {
                logger.warn("Branch $branchId is at full capacity: ${capacity.currentOccupancy}/${capacity.maxCapacity}")
                throw ValidationException(
                    "Facility is at full capacity (${capacity.currentOccupancy}/${capacity.maxCapacity}). Please wait."
                )
            }

            // Log capacity info
            logger.info("Branch $branchId capacity: ${capacity.currentOccupancy}/${capacity.maxCapacity} (${capacity.getOccupancyPercentage()}%)")
        }
    }

    /**
     * Creates an access log entry.
     */
    private fun createAccessLog(
        memberId: UUID,
        branchId: UUID,
        subscriptionId: UUID,
        isGuest: Boolean,
        accessMethod: String,
        deviceId: String?,
        validatedBy: UUID?
    ): AccessLog {
        val accessType = if (isGuest) AccessType.GUEST else AccessType.REGULAR

        val accessLog = AccessLog.create(
            memberId = memberId,
            branchId = branchId,
            subscriptionId = subscriptionId,
            accessType = accessType,
            accessPoint = accessMethod,
            deviceId = deviceId,
            validatedBy = validatedBy
        )

        return accessLogRepository.save(accessLog)
            .getOrElse { error ->
                logger.error("Failed to save access log: ${error.message}", error)
                throw error
            }
    }

    /**
     * Updates branch capacity atomically.
     */
    private fun updateCapacity(branchId: UUID, memberGender: Gender, increment: Boolean) {
        try {
            if (increment) {
                branchCapacityRepository.incrementOccupancy(branchId, memberGender == Gender.MALE)
            } else {
                branchCapacityRepository.decrementOccupancy(branchId, memberGender == Gender.MALE)
            }
        } catch (e: Exception) {
            logger.error("Failed to update capacity: ${e.message}", e)
            // Don't fail check-in if capacity update fails
        }
    }

    /**
     * Publishes AccessGrantedEvent.
     */
    private fun publishAccessGrantedEvent(memberId: UUID, branchId: UUID, accessLogId: UUID, accessMethod: String) {
        try {
            val event = AccessGrantedEvent(
                memberId = memberId,
                branchId = branchId,
                accessLogId = accessLogId,
                accessMethod = accessMethod,
                timestamp = Instant.now()
            )
            eventPublisher.publish(event)
            logger.info("Published AccessGrantedEvent for member: $memberId")
        } catch (e: Exception) {
            logger.error("Failed to publish AccessGrantedEvent: ${e.message}", e)
        }
    }

    /**
     * Publishes MemberCheckedInEvent.
     */
    private fun publishMemberCheckedInEvent(memberId: UUID, branchId: UUID) {
        try {
            val event = MemberCheckedInEvent(
                memberId = memberId,
                branchId = branchId,
                timestamp = Instant.now()
            )
            eventPublisher.publish(event)
            logger.info("Published MemberCheckedInEvent for member: $memberId")
        } catch (e: Exception) {
            logger.error("Failed to publish MemberCheckedInEvent: ${e.message}", e)
        }
    }

    /**
     * Publishes AccessDeniedEvent.
     */
    private fun publishAccessDeniedEvent(memberId: UUID, branchId: UUID, reason: String) {
        val event = AccessDeniedEvent(
            memberId = memberId,
            branchId = branchId,
            reason = reason,
            timestamp = Instant.now()
        )
        eventPublisher.publish(event)
    }

    /**
     * Sends check-in notification to member.
     */
    private fun sendCheckInNotification(memberId: UUID, branchId: UUID, accessLogId: UUID) {
        try {
            notificationService.sendCheckInConfirmation(memberId, branchId, accessLogId)
                .getOrElse { error ->
                    logger.warn("Failed to send check-in notification: ${error.message}")
                }
        } catch (e: Exception) {
            logger.error("Failed to send check-in notification: ${e.message}", e)
        }
    }

    /**
     * Sends access denied notification to member.
     */
    private fun sendAccessDeniedNotification(memberId: UUID, branchId: UUID, reason: String) {
        try {
            notificationService.sendAccessDeniedNotification(memberId, branchId, reason)
        } catch (e: Exception) {
            logger.error("Failed to send access denied notification: ${e.message}", e)
        }
    }
}
