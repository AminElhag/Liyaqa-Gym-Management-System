package com.liyaqa.gym.application.access.commands

import java.util.UUID

/**
 * Command for checking out a member from a branch.
 *
 * @property memberId The unique identifier of the member (can be null if using access log ID)
 * @property accessLogId The unique identifier of the access log (can be null if using member ID and branch ID)
 * @property branchId The unique identifier of the branch (required if using member ID)
 */
data class CheckOutCommand(
    val memberId: UUID?,
    val accessLogId: UUID?,
    val branchId: UUID?
) {
    init {
        require(accessLogId != null || (memberId != null && branchId != null)) {
            "Either accessLogId or both memberId and branchId must be provided"
        }
    }
}
