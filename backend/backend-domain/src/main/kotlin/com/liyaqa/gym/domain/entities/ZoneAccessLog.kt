package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.util.UUID

/**
 * ZoneAccessLog entity tracking member access to specific zones within a facility.
 */
data class ZoneAccessLog(
    val id: UUID,
    val accessLogId: UUID,
    val zoneId: UUID,
    val memberId: UUID,
    val entryTime: Instant,
    val exitTime: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        exitTime?.let {
            require(!it.isBefore(entryTime)) {
                "Exit time cannot be before entry time"
            }
        }
    }

    fun recordExit(): ZoneAccessLog {
        require(exitTime == null) { "Zone exit already recorded" }
        val now = Instant.now()
        return copy(exitTime = now, updatedAt = now)
    }

    companion object {
        fun create(
            accessLogId: UUID,
            zoneId: UUID,
            memberId: UUID
        ): ZoneAccessLog {
            val now = Instant.now()
            return ZoneAccessLog(
                id = UUID.randomUUID(),
                accessLogId = accessLogId,
                zoneId = zoneId,
                memberId = memberId,
                entryTime = now,
                exitTime = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
