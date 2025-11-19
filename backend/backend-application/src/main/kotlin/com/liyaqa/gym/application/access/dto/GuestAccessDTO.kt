package com.liyaqa.gym.application.access.dto

import com.liyaqa.gym.domain.entities.GuestAccess
import java.time.Instant
import java.util.UUID

/**
 * DTO representing guest access information.
 *
 * @property id The unique identifier of the guest access
 * @property hostMemberId The unique identifier of the host member
 * @property branchId The unique identifier of the branch
 * @property guestName The name of the guest
 * @property guestPhone The phone number of the guest
 * @property qrCode The QR code for guest access
 * @property validFrom The start time of validity
 * @property validUntil The end time of validity
 * @property isUsed Whether the guest access has been used
 * @property usedAt The time when the guest access was used
 */
data class GuestAccessDTO(
    val id: UUID,
    val hostMemberId: UUID,
    val branchId: UUID,
    val guestName: String,
    val guestPhone: String?,
    val qrCode: String,
    val validFrom: Instant,
    val validUntil: Instant,
    val isUsed: Boolean,
    val usedAt: Instant?
)

/**
 * Mapper for converting GuestAccess entity to DTO.
 */
object GuestAccessMapper {
    fun toDTO(guestAccess: GuestAccess): GuestAccessDTO {
        return GuestAccessDTO(
            id = guestAccess.id,
            hostMemberId = guestAccess.hostMemberId,
            branchId = guestAccess.branchId,
            guestName = guestAccess.guestName,
            guestPhone = guestAccess.guestPhone,
            qrCode = guestAccess.qrCode,
            validFrom = guestAccess.validFrom,
            validUntil = guestAccess.validUntil,
            isUsed = guestAccess.isUsed,
            usedAt = guestAccess.usedAt
        )
    }
}
