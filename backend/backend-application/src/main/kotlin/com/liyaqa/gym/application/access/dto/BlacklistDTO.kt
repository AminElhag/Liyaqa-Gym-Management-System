package com.liyaqa.gym.application.access.dto

import java.time.Instant
import java.util.UUID

/**
 * DTO representing blacklist information.
 *
 * @property blacklistId The unique identifier of the blacklist entry
 * @property memberId The unique identifier of the member
 * @property memberName The name of the member
 * @property branchId The unique identifier of the branch (null for system-wide)
 * @property reason The reason for blacklisting
 * @property blacklistedAt The time when member was blacklisted
 * @property expiresAt The expiration time of the blacklist (null for permanent)
 * @property message Confirmation message
 */
data class BlacklistDTO(
    val blacklistId: UUID,
    val memberId: UUID,
    val memberName: String,
    val branchId: UUID?,
    val reason: String,
    val blacklistedAt: Instant,
    val expiresAt: Instant?,
    val message: String = "Member access has been revoked"
)
