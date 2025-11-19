package com.liyaqa.gym.application.access.commands

import java.util.UUID

/**
 * Command for granting a member access to a specific zone.
 *
 * @property memberId The unique identifier of the member
 * @property zoneId The unique identifier of the zone
 * @property branchId The unique identifier of the branch
 */
data class GrantZoneAccessCommand(
    val memberId: UUID,
    val zoneId: UUID,
    val branchId: UUID
)
