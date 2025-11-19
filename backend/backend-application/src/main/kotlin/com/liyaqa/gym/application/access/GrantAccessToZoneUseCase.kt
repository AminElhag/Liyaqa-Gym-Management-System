package com.liyaqa.gym.application.access

import com.liyaqa.gym.application.access.commands.GrantZoneAccessCommand
import com.liyaqa.gym.application.access.dto.ZoneAccessDenied
import com.liyaqa.gym.application.access.dto.ZoneAccessGranted
import com.liyaqa.gym.application.access.dto.ZoneAccessResult
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.entities.MembershipPlan
import com.liyaqa.gym.domain.entities.Zone
import com.liyaqa.gym.domain.entities.ZoneAccessLog
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.ZoneAccessGrantedEvent
import com.liyaqa.gym.domain.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Use case for granting a member access to a specific zone within a facility.
 *
 * This use case handles:
 * - Member validation (currently at facility)
 * - Zone permissions checking based on membership plan
 * - Zone capacity checking
 * - Zone access log creation
 * - Event publishing
 *
 * @property memberRepository Repository for member operations
 * @property subscriptionRepository Repository for subscription operations
 * @property planRepository Repository for membership plan operations
 * @property zoneRepository Repository for zone operations
 * @property accessLogRepository Repository for access log operations
 * @property zoneAccessLogRepository Repository for zone access log operations
 * @property eventPublisher Publisher for domain events
 */
@Service
@Transactional
class GrantAccessToZoneUseCase(
    private val memberRepository: MemberRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val planRepository: MembershipPlanRepository,
    private val zoneRepository: ZoneRepository,
    private val accessLogRepository: AccessLogRepository,
    private val zoneAccessLogRepository: ZoneAccessLogRepository,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(GrantAccessToZoneUseCase::class.java)

    /**
     * Executes the grant zone access use case.
     *
     * @param command The grant zone access command
     * @return Result containing zone access granted or denied
     */
    fun execute(command: GrantZoneAccessCommand): Result<ZoneAccessResult> {
        return runCatching {
            logger.info("Processing zone access for member: ${command.memberId}, zone: ${command.zoneId}")

            // 1. Validate member exists
            val member = getMember(command.memberId)

            // 2. Validate zone exists and is active
            val zone = validateZone(command.zoneId)

            // 3. Validate member is currently at the facility
            val activeAccessLog = validateMemberAtFacility(command.memberId, command.branchId)

            // 4. Get member's subscription and plan
            val subscription = getActiveSubscription(command.memberId)
            val plan = getPlan(subscription.planId)

            // 5. Check zone permissions based on plan
            validateZonePermissions(member, plan, zone)

            // 6. Create zone access log entry
            val zoneAccessLog = createZoneAccessLog(
                activeAccessLog.id,
                command.zoneId,
                command.memberId
            )

            // 7. Publish event
            publishZoneAccessGrantedEvent(command.memberId, command.zoneId, activeAccessLog.id)

            logger.info("Zone access granted for member: ${command.memberId} to zone: ${zone.name}")

            // 8. Return success result
            ZoneAccessGranted(
                memberId = command.memberId,
                zoneId = command.zoneId,
                timestamp = zoneAccessLog.entryTime,
                zoneName = zone.name,
                zoneAccessLogId = zoneAccessLog.id,
                message = "Access granted to ${zone.name}"
            ) as ZoneAccessResult

        }.recoverCatching { error ->
            logger.error("Zone access failed: ${error.message}", error)

            val zoneName = try {
                val zoneOpt = zoneRepository.findById(command.zoneId).getOrNull()
                zoneOpt?.get()?.name ?: "Unknown Zone"
            } catch (e: Exception) {
                "Unknown Zone"
            }

            val reason = when (error) {
                is ValidationException -> error.message ?: "Validation failed"
                is ResourceNotFoundException -> error.message ?: "Resource not found"
                else -> "Zone access failed: ${error.message}"
            }

            ZoneAccessDenied(
                memberId = command.memberId,
                zoneId = command.zoneId,
                timestamp = Instant.now(),
                zoneName = zoneName,
                reason = reason
            ) as ZoneAccessResult
        }
    }

    /**
     * Gets and validates member.
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
     * Validates zone exists and is active.
     */
    private fun validateZone(zoneId: UUID): Zone {
        val zoneOpt = zoneRepository.findById(zoneId)
            .getOrElse { error ->
                logger.error("Failed to query zone repository: ${error.message}", error)
                throw error
            }

        if (!zoneOpt.isPresent) {
            logger.warn("Zone not found: $zoneId")
            throw ResourceNotFoundException("Zone with ID $zoneId not found")
        }

        val zone = zoneOpt.get()

        if (!zone.isActive) {
            logger.warn("Attempted access to inactive zone: $zoneId")
            throw ValidationException("Zone ${zone.name} is currently inactive")
        }

        return zone
    }

    /**
     * Validates member is currently checked in at the facility.
     */
    private fun validateMemberAtFacility(memberId: UUID, branchId: UUID): com.liyaqa.gym.domain.entities.AccessLog {
        val activeAccessOpt = accessLogRepository.findActiveByMemberAndBranch(memberId, branchId)
            .getOrElse { error ->
                logger.error("Failed to query active access: ${error.message}", error)
                throw error
            }

        if (!activeAccessOpt.isPresent) {
            logger.warn("Member $memberId is not currently at facility $branchId")
            throw ValidationException("You must be checked in at the facility to access zones")
        }

        return activeAccessOpt.get()
    }

    /**
     * Gets active subscription.
     */
    private fun getActiveSubscription(memberId: UUID): com.liyaqa.gym.domain.entities.Subscription {
        val subscriptions = subscriptionRepository.findByMember(memberId)
            .getOrElse { error ->
                logger.error("Failed to query subscriptions: ${error.message}", error)
                throw error
            }

        val activeSubscription = subscriptions.firstOrNull {
            it.status == com.liyaqa.gym.domain.entities.SubscriptionStatus.ACTIVE
        }

        if (activeSubscription == null) {
            logger.warn("Member $memberId has no active subscription")
            throw ValidationException("No active subscription found")
        }

        return activeSubscription
    }

    /**
     * Gets membership plan.
     */
    private fun getPlan(planId: UUID): MembershipPlan {
        val planOpt = planRepository.findById(planId)
            .getOrElse { error ->
                logger.error("Failed to query plan repository: ${error.message}", error)
                throw error
            }

        if (!planOpt.isPresent) {
            logger.warn("Plan not found: $planId")
            throw ResourceNotFoundException("Membership plan with ID $planId not found")
        }

        return planOpt.get()
    }

    /**
     * Validates zone permissions based on membership plan.
     */
    private fun validateZonePermissions(member: Member, plan: MembershipPlan, zone: Zone) {
        // Check if zone allows member's gender
        if (zone.genderRestriction != null && zone.genderRestriction != member.gender) {
            logger.warn("Gender restriction violated for zone ${zone.id}: member ${member.gender}, zone requires ${zone.genderRestriction}")
            throw ValidationException("Zone ${zone.name} is restricted to ${zone.genderRestriction} members only")
        }

        // Check if plan includes required features for zone access
        val hasRequiredFeatures = zone.requiredFeatures.all { required ->
            plan.features.contains(required)
        }

        if (!hasRequiredFeatures) {
            val missingFeatures = zone.requiredFeatures.filter { !plan.features.contains(it) }
            logger.warn("Member ${member.id} lacks required features for zone ${zone.id}: $missingFeatures")
            throw ValidationException(
                "Your membership plan does not include access to ${zone.name}. " +
                        "Required features: ${missingFeatures.joinToString(", ")}"
            )
        }

        logger.debug("Zone permission validation passed for member ${member.id} to zone ${zone.id}")
    }

    /**
     * Creates zone access log entry.
     */
    private fun createZoneAccessLog(
        accessLogId: UUID,
        zoneId: UUID,
        memberId: UUID
    ): ZoneAccessLog {
        val zoneAccessLog = ZoneAccessLog.create(
            accessLogId = accessLogId,
            zoneId = zoneId,
            memberId = memberId
        )

        return zoneAccessLogRepository.save(zoneAccessLog)
            .getOrElse { error ->
                logger.error("Failed to save zone access log: ${error.message}", error)
                throw error
            }
    }

    /**
     * Publishes ZoneAccessGrantedEvent.
     */
    private fun publishZoneAccessGrantedEvent(memberId: UUID, zoneId: UUID, accessLogId: UUID) {
        try {
            val event = ZoneAccessGrantedEvent(
                memberId = memberId,
                zoneId = zoneId,
                accessLogId = accessLogId,
                timestamp = Instant.now()
            )
            eventPublisher.publish(event)
            logger.info("Published ZoneAccessGrantedEvent for member: $memberId")
        } catch (e: Exception) {
            logger.error("Failed to publish ZoneAccessGrantedEvent: ${e.message}", e)
        }
    }
}
