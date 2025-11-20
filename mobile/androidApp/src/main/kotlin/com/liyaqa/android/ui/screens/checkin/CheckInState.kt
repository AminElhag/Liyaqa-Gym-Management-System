package com.liyaqa.android.ui.screens.checkin

import kotlinx.datetime.Instant

/**
 * State for the check-in screen
 */
sealed class CheckInState {
    /**
     * Initial state, QR code is being generated
     */
    data object Loading : CheckInState()

    /**
     * QR code is ready to be displayed
     */
    data class QRCodeReady(
        val qrCodeData: String,
        val expiresAt: Instant,
        val memberName: String,
        val branchId: String
    ) : CheckInState()

    /**
     * Check-in is in progress
     */
    data object CheckingIn : CheckInState()

    /**
     * Check-in was successful
     */
    data class CheckedIn(
        val checkInTime: Instant,
        val message: String = "Successfully checked in!"
    ) : CheckInState()

    /**
     * Error occurred during check-in
     */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : CheckInState()

    /**
     * Currently checked out (after a successful check-in)
     */
    data class CheckedOut(
        val checkOutTime: Instant,
        val message: String = "Successfully checked out!"
    ) : CheckInState()
}

/**
 * Check-in status for UI updates
 */
sealed class CheckInStatus {
    /**
     * Idle state (no action in progress)
     */
    data object Idle : CheckInStatus()

    /**
     * Check-in in progress
     */
    data object InProgress : CheckInStatus()

    /**
     * Check-in succeeded
     */
    data class Success(
        val time: Instant,
        val message: String = "Checked in successfully!"
    ) : CheckInStatus()

    /**
     * Check-in failed
     */
    data class Error(
        val message: String
    ) : CheckInStatus()
}

/**
 * QR Code data model
 */
data class QRCodeData(
    val memberId: String,
    val branchId: String,
    val timestamp: Long,
    val nonce: String,
    val signature: String? = null
) {
    /**
     * Convert to a string format for encoding in QR code
     * Format: memberId|branchId|timestamp|nonce|signature
     */
    fun toEncodedString(): String {
        return buildString {
            append(memberId)
            append("|")
            append(branchId)
            append("|")
            append(timestamp)
            append("|")
            append(nonce)
            if (!signature.isNullOrEmpty()) {
                append("|")
                append(signature)
            }
        }
    }

    companion object {
        /**
         * Parse encoded string back to QRCodeData
         */
        fun fromEncodedString(encoded: String): QRCodeData? {
            return try {
                val parts = encoded.split("|")
                if (parts.size < 4) return null

                QRCodeData(
                    memberId = parts[0],
                    branchId = parts[1],
                    timestamp = parts[2].toLong(),
                    nonce = parts[3],
                    signature = parts.getOrNull(4)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
