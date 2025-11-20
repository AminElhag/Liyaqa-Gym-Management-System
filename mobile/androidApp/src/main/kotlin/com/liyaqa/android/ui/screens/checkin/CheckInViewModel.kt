package com.liyaqa.android.ui.screens.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liyaqa.gym.domain.usecases.CheckInUseCase
import com.liyaqa.gym.domain.usecases.GetMemberProfileUseCase
import com.liyaqa.gym.network.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel for the Check-In screen
 * Manages QR code generation, check-in state, and offline queueing
 */
@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val getMemberProfileUseCase: GetMemberProfileUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _checkInState = MutableStateFlow<CheckInState>(CheckInState.Loading)
    val checkInState: StateFlow<CheckInState> = _checkInState.asStateFlow()

    private val _checkInStatus = MutableStateFlow<CheckInStatus>(CheckInStatus.Idle)
    val checkInStatus: StateFlow<CheckInStatus> = _checkInStatus.asStateFlow()

    private val _qrCodeData = MutableStateFlow("")
    val qrCodeData: StateFlow<String> = _qrCodeData.asStateFlow()

    private val _isCheckedIn = MutableStateFlow(false)
    val isCheckedIn: StateFlow<Boolean> = _isCheckedIn.asStateFlow()

    // Offline queue for check-ins
    private val offlineCheckInQueue = mutableListOf<QRCodeData>()

    private var currentQRCodeData: QRCodeData? = null
    private var lastCheckInTime: Instant? = null

    // QR Code refresh interval (30 seconds)
    private val qrCodeRefreshInterval = 30.seconds

    init {
        loadMemberDataAndGenerateQRCode()
        startQRCodeAutoRefresh()
    }

    /**
     * Load member data and generate initial QR code
     */
    private fun loadMemberDataAndGenerateQRCode() {
        viewModelScope.launch {
            try {
                _checkInState.value = CheckInState.Loading

                val memberId = tokenStorage.getMemberId()
                if (memberId == null) {
                    _checkInState.value = CheckInState.Error(
                        message = "User not authenticated",
                        canRetry = false
                    )
                    return@launch
                }

                // Get member profile
                val memberResult = getMemberProfileUseCase(memberId, forceRefresh = false)
                if (memberResult.isFailure) {
                    _checkInState.value = CheckInState.Error(
                        message = "Failed to load member profile",
                        canRetry = true
                    )
                    return@launch
                }

                val member = memberResult.getOrThrow()

                // Generate QR code
                generateQRCode(memberId, member.branchId, member.name)

            } catch (e: Exception) {
                _checkInState.value = CheckInState.Error(
                    message = e.message ?: "An unexpected error occurred",
                    canRetry = true
                )
            }
        }
    }

    /**
     * Generate a new QR code with current timestamp and nonce
     */
    private fun generateQRCode(memberId: String, branchId: String, memberName: String) {
        val now = Clock.System.now()
        val timestamp = now.toEpochMilliseconds()
        val nonce = generateNonce()
        val signature = generateSignature(memberId, branchId, timestamp, nonce)

        currentQRCodeData = QRCodeData(
            memberId = memberId,
            branchId = branchId,
            timestamp = timestamp,
            nonce = nonce,
            signature = signature
        )

        _qrCodeData.value = currentQRCodeData!!.toEncodedString()

        _checkInState.value = CheckInState.QRCodeReady(
            qrCodeData = _qrCodeData.value,
            expiresAt = now.plus(qrCodeRefreshInterval),
            memberName = memberName,
            branchId = branchId
        )
    }

    /**
     * Refresh QR code (called automatically every 30 seconds or manually)
     */
    fun refreshQRCode() {
        viewModelScope.launch {
            try {
                val memberId = tokenStorage.getMemberId() ?: return@launch
                val currentState = _checkInState.value

                if (currentState is CheckInState.QRCodeReady) {
                    generateQRCode(memberId, currentState.branchId, currentState.memberName)
                } else if (currentState is CheckInState.Error && currentState.canRetry) {
                    loadMemberDataAndGenerateQRCode()
                }
            } catch (e: Exception) {
                // Silently fail QR code refresh, keep showing the old one
            }
        }
    }

    /**
     * Start auto-refresh timer for QR code
     */
    private fun startQRCodeAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(qrCodeRefreshInterval)
                refreshQRCode()
            }
        }
    }

    /**
     * Perform manual check-in
     */
    fun checkIn() {
        viewModelScope.launch {
            try {
                _checkInStatus.value = CheckInStatus.InProgress
                _checkInState.value = CheckInState.CheckingIn

                val qrData = currentQRCodeData
                if (qrData == null) {
                    _checkInStatus.value = CheckInStatus.Error("QR code not ready")
                    _checkInState.value = CheckInState.Error("QR code not ready")
                    return@launch
                }

                // Simulate check-in API call
                // In a real implementation, this would call the backend API with QR code data
                // For now, we'll simulate a successful check-in
                delay(1000) // Simulate network delay

                val checkInTime = Clock.System.now()
                lastCheckInTime = checkInTime
                _isCheckedIn.value = true

                _checkInStatus.value = CheckInStatus.Success(
                    time = checkInTime,
                    message = "Successfully checked in!"
                )

                _checkInState.value = CheckInState.CheckedIn(
                    checkInTime = checkInTime,
                    message = "Welcome! You're checked in."
                )

                // TODO: Implement actual API call when backend is ready
                // val result = checkInUseCase.invoke(bookingId)
                // Handle result accordingly

            } catch (e: Exception) {
                // If offline, queue the check-in
                val qrData = currentQRCodeData
                if (qrData != null && isNetworkError(e)) {
                    queueOfflineCheckIn(qrData)
                    _checkInStatus.value = CheckInStatus.Success(
                        time = Clock.System.now(),
                        message = "Queued for check-in (offline)"
                    )
                } else {
                    _checkInStatus.value = CheckInStatus.Error(
                        e.message ?: "Check-in failed"
                    )
                    _checkInState.value = CheckInState.Error(
                        message = e.message ?: "Check-in failed",
                        canRetry = true
                    )
                }
            }
        }
    }

    /**
     * Perform check-out
     */
    fun checkOut() {
        viewModelScope.launch {
            try {
                _checkInStatus.value = CheckInStatus.InProgress

                // Simulate check-out API call
                delay(1000)

                val checkOutTime = Clock.System.now()
                _isCheckedIn.value = false
                lastCheckInTime = null

                _checkInState.value = CheckInState.CheckedOut(
                    checkOutTime = checkOutTime,
                    message = "Thank you! See you next time."
                )

                // Reset to QR code ready state after 3 seconds
                delay(3000)
                refreshQRCode()

            } catch (e: Exception) {
                _checkInStatus.value = CheckInStatus.Error(
                    e.message ?: "Check-out failed"
                )
            }
        }
    }

    /**
     * Queue check-in for offline processing
     */
    private fun queueOfflineCheckIn(qrCodeData: QRCodeData) {
        offlineCheckInQueue.add(qrCodeData)
        // TODO: Persist queue to local storage for durability
        // TODO: Implement background sync when network is available
    }

    /**
     * Process queued offline check-ins
     */
    fun processOfflineQueue() {
        viewModelScope.launch {
            if (offlineCheckInQueue.isEmpty()) return@launch

            val queueCopy = offlineCheckInQueue.toList()
            offlineCheckInQueue.clear()

            queueCopy.forEach { qrData ->
                try {
                    // TODO: Implement actual API call to process queued check-in
                    // For now, just simulate success
                    delay(500)
                } catch (e: Exception) {
                    // Re-queue if still failing
                    offlineCheckInQueue.add(qrData)
                }
            }
        }
    }

    /**
     * Reset check-in status
     */
    fun resetStatus() {
        _checkInStatus.value = CheckInStatus.Idle
    }

    /**
     * Generate a random nonce for QR code security
     */
    private fun generateNonce(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * Generate a signature for QR code data
     * In production, this should use proper cryptographic signing
     */
    private fun generateSignature(
        memberId: String,
        branchId: String,
        timestamp: Long,
        nonce: String
    ): String {
        val data = "$memberId$branchId$timestamp$nonce"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }.take(16)
    }

    /**
     * Check if an exception is a network error
     */
    private fun isNetworkError(exception: Exception): Boolean {
        // Simple heuristic - in production, check for specific network exception types
        return exception.message?.contains("network", ignoreCase = true) == true ||
                exception.message?.contains("connection", ignoreCase = true) == true
    }

    /**
     * Get the number of queued offline check-ins
     */
    fun getOfflineQueueSize(): Int = offlineCheckInQueue.size
}
