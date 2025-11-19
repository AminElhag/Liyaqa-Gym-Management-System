package com.liyaqa.gym.application.access.dto

import java.time.Instant
import java.util.UUID

/**
 * DTO representing the result of an access grant or denial.
 */
sealed class AccessResult {
    abstract val memberId: UUID
    abstract val branchId: UUID
    abstract val timestamp: Instant
}

/**
 * Access was granted successfully.
 */
data class AccessGranted(
    override val memberId: UUID,
    override val branchId: UUID,
    override val timestamp: Instant,
    val accessLogId: UUID,
    val memberName: String,
    val subscriptionEndDate: java.time.LocalDate?,
    val message: String = "Access granted successfully"
) : AccessResult()

/**
 * Access was denied.
 */
data class AccessDenied(
    override val memberId: UUID,
    override val branchId: UUID,
    override val timestamp: Instant,
    val reason: String,
    val details: String? = null
) : AccessResult()
