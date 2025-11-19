package com.liyaqa.gym.application.access.dto

import java.time.Instant
import java.util.UUID

/**
 * DTO representing zone access result.
 */
sealed class ZoneAccessResult {
    abstract val memberId: UUID
    abstract val zoneId: UUID
    abstract val timestamp: Instant
}

/**
 * Zone access granted.
 */
data class ZoneAccessGranted(
    override val memberId: UUID,
    override val zoneId: UUID,
    override val timestamp: Instant,
    val zoneName: String,
    val zoneAccessLogId: UUID,
    val message: String = "Zone access granted"
) : ZoneAccessResult()

/**
 * Zone access denied.
 */
data class ZoneAccessDenied(
    override val memberId: UUID,
    override val zoneId: UUID,
    override val timestamp: Instant,
    val zoneName: String,
    val reason: String
) : ZoneAccessResult()
