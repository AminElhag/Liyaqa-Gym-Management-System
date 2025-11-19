package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.util.UUID

/**
 * GuestAccess entity for temporary access codes for guests.
 */
data class GuestAccess(
    val id: UUID,
    val hostMemberId: UUID,
    val branchId: UUID,
    val guestName: String,
    val guestPhone: String?,
    val qrCode: String,
    val validFrom: Instant,
    val validUntil: Instant,
    val isUsed: Boolean,
    val usedAt: Instant?,
    val accessLogId: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(guestName.isNotBlank()) { "Guest name cannot be blank" }
        require(qrCode.isNotBlank()) { "QR code cannot be blank" }
        require(validUntil.isAfter(validFrom)) {
            "Valid until must be after valid from"
        }
    }

    fun isValid(): Boolean {
        val now = Instant.now()
        return !isUsed && now.isAfter(validFrom) && now.isBefore(validUntil)
    }

    fun isExpired(): Boolean {
        return Instant.now().isAfter(validUntil)
    }

    fun markAsUsed(accessLogId: UUID): GuestAccess {
        require(!isUsed) { "Guest access has already been used" }
        require(isValid()) { "Guest access is not valid or expired" }
        val now = Instant.now()
        return copy(
            isUsed = true,
            usedAt = now,
            accessLogId = accessLogId,
            updatedAt = now
        )
    }

    companion object {
        fun create(
            hostMemberId: UUID,
            branchId: UUID,
            guestName: String,
            guestPhone: String?,
            validityHours: Long = 24
        ): GuestAccess {
            val now = Instant.now()
            val validUntil = now.plusSeconds(validityHours * 3600)

            // Generate a simple QR code identifier (in real implementation, use proper QR code generation)
            val qrCode = "GUEST-${UUID.randomUUID()}"

            return GuestAccess(
                id = UUID.randomUUID(),
                hostMemberId = hostMemberId,
                branchId = branchId,
                guestName = guestName,
                guestPhone = guestPhone,
                qrCode = qrCode,
                validFrom = now,
                validUntil = validUntil,
                isUsed = false,
                usedAt = null,
                accessLogId = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
