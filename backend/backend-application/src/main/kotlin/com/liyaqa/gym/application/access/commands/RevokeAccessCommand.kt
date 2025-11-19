package com.liyaqa.gym.application.access.commands

import java.time.Instant
import java.util.UUID

/**
 * Command for revoking access and blacklisting a member.
 *
 * @property memberId The unique identifier of the member
 * @property branchId The unique identifier of the branch (null for system-wide blacklist)
 * @property reason The reason for revoking access
 * @property revokedBy The staff member who revoked access
 * @property expiresAt Optional expiration date for temporary blacklist
 * @property notes Additional notes about the revocation
 * @property forceCheckOut Whether to force checkout if member is currently checked in
 */
data class RevokeAccessCommand(
    val memberId: UUID,
    val branchId: UUID?,
    val reason: String,
    val revokedBy: UUID,
    val expiresAt: Instant? = null,
    val notes: String? = null,
    val forceCheckOut: Boolean = true
) {
    init {
        require(reason.isNotBlank()) {
            "Reason for revoking access cannot be blank"
        }
    }
}
