package com.liyaqa.gym.domain.entities

import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * AccessLog entity tracking member check-ins and check-outs.
 * Records when members enter and exit the facility.
 */
data class AccessLog(
    val id: UUID,
    val memberId: UUID,
    val branchId: UUID,
    val subscriptionId: UUID?,
    val checkInTime: Instant,
    val checkOutTime: Instant?,
    val accessType: AccessType,
    val accessPoint: String?,
    val deviceId: String?,
    val notes: String?,
    val validatedBy: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        checkOutTime?.let {
            require(!it.isBefore(checkInTime)) {
                "Check-out time cannot be before check-in time"
            }
        }
    }

    fun isCheckedOut(): Boolean = checkOutTime != null

    fun isStillInside(): Boolean = checkOutTime == null

    fun getDuration(): Duration? {
        return checkOutTime?.let { Duration.between(checkInTime, it) }
    }

    fun getDurationMinutes(): Long? {
        return getDuration()?.toMinutes()
    }

    fun checkOut(): AccessLog {
        require(checkOutTime == null) { "Already checked out" }
        val now = Instant.now()
        return copy(checkOutTime = now, updatedAt = now)
    }

    fun updateCheckOutTime(newCheckOutTime: Instant): AccessLog {
        require(!newCheckOutTime.isBefore(checkInTime)) {
            "Check-out time cannot be before check-in time"
        }
        return copy(checkOutTime = newCheckOutTime, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            memberId: UUID,
            branchId: UUID,
            subscriptionId: UUID?,
            accessType: AccessType,
            accessPoint: String? = null,
            deviceId: String? = null,
            validatedBy: UUID? = null
        ): AccessLog {
            val now = Instant.now()
            return AccessLog(
                id = UUID.randomUUID(),
                memberId = memberId,
                branchId = branchId,
                subscriptionId = subscriptionId,
                checkInTime = now,
                checkOutTime = null,
                accessType = accessType,
                accessPoint = accessPoint,
                deviceId = deviceId,
                notes = null,
                validatedBy = validatedBy,
                createdAt = now,
                updatedAt = now
            )
        }

        fun createWithCheckOut(
            memberId: UUID,
            branchId: UUID,
            subscriptionId: UUID?,
            checkInTime: Instant,
            checkOutTime: Instant,
            accessType: AccessType
        ): AccessLog {
            require(!checkOutTime.isBefore(checkInTime)) {
                "Check-out time cannot be before check-in time"
            }
            val now = Instant.now()
            return AccessLog(
                id = UUID.randomUUID(),
                memberId = memberId,
                branchId = branchId,
                subscriptionId = subscriptionId,
                checkInTime = checkInTime,
                checkOutTime = checkOutTime,
                accessType = accessType,
                accessPoint = null,
                deviceId = null,
                notes = null,
                validatedBy = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Access type enumeration
 */
enum class AccessType {
    REGULAR,
    CLASS_BOOKING,
    PT_SESSION,
    GUEST,
    STAFF,
    TRIAL
}
