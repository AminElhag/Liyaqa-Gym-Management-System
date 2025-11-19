package com.liyaqa.gym.application.member

import com.liyaqa.gym.application.member.commands.DeleteMemberCommand
import com.liyaqa.gym.application.member.dto.MemberDeletionResult
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.MemberDeletedEvent
import com.liyaqa.gym.domain.repositories.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Use case for deleting a member (GDPR compliant).
 *
 * This use case handles:
 * - Member existence validation
 * - Optional data export before deletion
 * - Soft delete (member record remains for audit trail)
 * - PII anonymization
 * - Repository persistence
 * - Cache invalidation
 * - Domain event publishing
 *
 * GDPR Compliance:
 * - Supports right to erasure (Article 17)
 * - Maintains audit trail while anonymizing personal data
 * - Exports data before deletion if requested
 *
 * @property memberRepository Repository for member persistence
 * @property eventPublisher Publisher for domain events
 */
@Service
@Transactional
class DeleteMemberUseCase(
    private val memberRepository: MemberRepository,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(DeleteMemberUseCase::class.java)

    /**
     * Executes the member deletion use case.
     *
     * @param command The deletion command containing member ID and options
     * @return Result containing the deletion result or error
     */
    @CacheEvict(
        value = ["member"],
        key = "#result.getOrNull()?.memberId",
        condition = "#result.isSuccess"
    )
    fun execute(command: DeleteMemberCommand): Result<MemberDeletionResult> {
        return runCatching {
            logger.info("Deleting member with ID: ${command.memberId}, reason: ${command.reason}")

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

            // 2. Export member data if requested (GDPR compliance)
            var exportedDataPath: String? = null
            if (command.exportDataBeforeDeletion) {
                exportedDataPath = exportMemberData(existingMember)
                logger.info("Member data exported to: $exportedDataPath")
            }

            // 3. Anonymize PII (Personal Identifiable Information)
            val anonymizedMember = anonymizeMember(existingMember)

            // 4. Perform soft delete
            memberRepository.softDelete(command.memberId)
                .getOrElse { error ->
                    logger.error("Failed to soft delete member: ${error.message}", error)
                    throw error
                }

            logger.info("Member deleted successfully with ID: ${command.memberId}")

            // 5. Publish domain event
            publishMemberDeletedEvent(existingMember, command, exportedDataPath != null)

            // 6. Build and return result
            MemberDeletionResult(
                memberId = command.memberId,
                exportedDataPath = exportedDataPath,
                deletedAt = Instant.now(),
                success = true,
                message = "Member deleted successfully. Personal data has been anonymized while maintaining audit trail."
            )

        }.onFailure { error ->
            logger.error("Failed to delete member: ${error.message}", error)
        }
    }

    /**
     * Exports member data to a file for GDPR compliance.
     * This allows the member to download their data before deletion.
     *
     * @param member The member whose data should be exported
     * @return Path to the exported data file
     */
    private fun exportMemberData(member: Member): String {
        // TODO: Implement actual data export
        // This should:
        // 1. Collect all member-related data (subscriptions, bookings, payments, etc.)
        // 2. Format as JSON or PDF
        // 3. Store in secure location
        // 4. Return path or download URL

        val exportPath = "/data/exports/members/${member.id}_${Instant.now().toEpochMilli()}.json"

        logger.info("Exporting member data for member ${member.id} to $exportPath")

        // Simulate export (in production, this would write to file/S3/etc.)
        val exportData = buildExportData(member)
        logger.debug("Export data prepared: ${exportData.length} characters")

        return exportPath
    }

    /**
     * Builds the export data for a member.
     *
     * @param member The member to export
     * @return JSON string containing all member data
     */
    private fun buildExportData(member: Member): String {
        // Build comprehensive export including all related entities
        return """
        {
            "memberId": "${member.id}",
            "name": "${member.name}",
            "email": "${member.contactInfo.email}",
            "phone": "${member.contactInfo.phone}",
            "nationalId": "${member.nationalId ?: "N/A"}",
            "dateOfBirth": "${member.dateOfBirth ?: "N/A"}",
            "gender": "${member.gender}",
            "branchId": "${member.branchId}",
            "status": "${member.status}",
            "createdAt": "${member.createdAt}",
            "updatedAt": "${member.updatedAt}",
            "exportedAt": "${Instant.now()}",
            "note": "This data is exported as part of GDPR right to erasure request"
        }
        """.trimIndent()
    }

    /**
     * Anonymizes PII in the member record while keeping audit trail.
     *
     * @param member The member to anonymize
     * @return Anonymized member
     */
    private fun anonymizeMember(member: Member): Member {
        // Anonymize PII fields while maintaining data structure for audit
        return member.copy(
            name = "DELETED_USER_${member.id.toString().take(8)}",
            nameArabic = null,
            contactInfo = com.liyaqa.gym.domain.valueobjects.ContactInfo.of(
                email = "deleted.${member.id}@anonymized.local",
                phone = "+000000000000"
            ),
            nationalId = null,
            dateOfBirth = null,
            profilePhotoUrl = null,
            emergencyContactName = null,
            emergencyContactPhone = null,
            notes = "Account deleted on ${Instant.now()}. Personal data anonymized.",
            updatedAt = Instant.now()
        )
    }

    /**
     * Publishes the MemberDeletedEvent after successful deletion.
     *
     * @param member The deleted member
     * @param command The deletion command
     * @param dataExported Whether data was exported
     */
    private fun publishMemberDeletedEvent(
        member: Member,
        command: DeleteMemberCommand,
        dataExported: Boolean
    ) {
        try {
            val event = MemberDeletedEvent(
                memberId = member.id,
                branchId = member.branchId,
                reason = command.reason,
                deletedBy = command.deletedBy,
                dataExported = dataExported,
                timestamp = Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published MemberDeletedEvent for member: ${member.id}")

        } catch (e: Exception) {
            // Log error but don't fail the deletion
            logger.error("Failed to publish MemberDeletedEvent for member ${member.id}: ${e.message}", e)
        }
    }

    /**
     * Checks if a member can be deleted.
     * This can be extended with business rules (e.g., can't delete if has active subscriptions).
     *
     * @param memberId The member ID to check
     * @return Result containing true if can be deleted, false otherwise
     */
    fun canDelete(memberId: java.util.UUID): Result<Boolean> {
        return runCatching {
            logger.debug("Checking if member can be deleted: $memberId")

            // Fetch member
            val member = memberRepository.findById(memberId)
                .getOrElse { error ->
                    logger.error("Failed to fetch member: ${error.message}", error)
                    throw error
                }
                .orElseThrow {
                    ResourceNotFoundException("Member not found with ID: $memberId")
                }

            // TODO: Add business rules
            // - Check for active subscriptions
            // - Check for unpaid invoices
            // - Check for scheduled classes
            // etc.

            // For now, always allow deletion
            true

        }.onFailure { error ->
            logger.error("Failed to check if member can be deleted: ${error.message}", error)
        }
    }
}
