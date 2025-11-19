package com.liyaqa.gym.application.member.commands

import java.util.UUID

/**
 * Command to suspend a member's account.
 * Suspended members cannot access gym facilities or book classes.
 *
 * @property memberId The ID of the member to suspend
 * @property reason The reason for suspension (required for audit trail)
 * @property suspendedBy The ID of the user performing the suspension
 * @property notifyMember Whether to send a notification to the member
 */
data class SuspendMemberCommand(
    val memberId: UUID,
    val reason: String,
    val suspendedBy: UUID,
    val notifyMember: Boolean = true
) {
    init {
        require(reason.isNotBlank()) { "Suspension reason is required and cannot be blank" }
        require(reason.length >= 10) { "Suspension reason must be at least 10 characters long" }
    }
}
