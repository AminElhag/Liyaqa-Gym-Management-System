package com.liyaqa.gym.application.access.commands

import java.util.UUID

/**
 * Command for checking in a member to a branch.
 *
 * @property memberId The unique identifier of the member (can be null if using QR code)
 * @property qrCode The QR code for check-in (can be null if using member ID)
 * @property branchId The unique identifier of the branch
 * @property accessMethod The method of access (QR, CARD, MANUAL, FACIAL, etc.)
 * @property deviceId The identifier of the access control device (optional)
 * @property validatedBy The staff member who validated the access (for manual validation)
 */
data class CheckInCommand(
    val memberId: UUID?,
    val qrCode: String?,
    val branchId: UUID,
    val accessMethod: String,
    val deviceId: String? = null,
    val validatedBy: UUID? = null
) {
    init {
        require(memberId != null || qrCode != null) {
            "Either memberId or qrCode must be provided"
        }
        require(accessMethod.isNotBlank()) {
            "Access method cannot be blank"
        }
    }
}
