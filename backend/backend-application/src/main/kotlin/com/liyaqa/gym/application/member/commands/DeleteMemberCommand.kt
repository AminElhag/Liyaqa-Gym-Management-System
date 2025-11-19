package com.liyaqa.gym.application.member.commands

import java.util.UUID

/**
 * Command to delete a member's account (GDPR compliant).
 * This performs a soft delete and anonymizes personal data while keeping audit trail.
 *
 * @property memberId The ID of the member to delete
 * @property reason The reason for deletion (required for audit)
 * @property deletedBy The ID of the user performing the deletion (null if self-deletion)
 * @property exportDataBeforeDeletion Whether to export member data before deletion
 */
data class DeleteMemberCommand(
    val memberId: UUID,
    val reason: String,
    val deletedBy: UUID? = null,
    val exportDataBeforeDeletion: Boolean = true
) {
    init {
        require(reason.isNotBlank()) { "Deletion reason is required and cannot be blank" }
        require(reason.length >= 10) { "Deletion reason must be at least 10 characters long" }
    }
}
