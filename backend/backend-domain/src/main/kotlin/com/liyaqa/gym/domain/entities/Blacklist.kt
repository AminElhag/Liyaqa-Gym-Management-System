package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.util.UUID

/**
 * Blacklist entity for members who are denied access.
 */
data class Blacklist(
    val id: UUID,
    val memberId: UUID,
    val branchId: UUID?,
    val reason: String,
    val blacklistedAt: Instant,
    val blacklistedBy: UUID,
    val expiresAt: Instant?,
    val notes: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(reason.isNotBlank()) { "Blacklist reason cannot be blank" }
        expiresAt?.let {
            require(!it.isBefore(blacklistedAt)) {
                "Expiration date cannot be before blacklist date"
            }
        }
    }

    fun isExpired(): Boolean {
        return expiresAt?.isBefore(Instant.now()) == true
    }

    fun isCurrentlyActive(): Boolean {
        return isActive && !isExpired()
    }

    fun revoke(): Blacklist {
        return copy(isActive = false, updatedAt = Instant.now())
    }

    fun extend(newExpiresAt: Instant): Blacklist {
        require(newExpiresAt.isAfter(Instant.now())) {
            "New expiration date must be in the future"
        }
        return copy(expiresAt = newExpiresAt, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            memberId: UUID,
            branchId: UUID?,
            reason: String,
            blacklistedBy: UUID,
            expiresAt: Instant? = null,
            notes: String? = null
        ): Blacklist {
            val now = Instant.now()
            return Blacklist(
                id = UUID.randomUUID(),
                memberId = memberId,
                branchId = branchId,
                reason = reason,
                blacklistedAt = now,
                blacklistedBy = blacklistedBy,
                expiresAt = expiresAt,
                notes = notes,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
