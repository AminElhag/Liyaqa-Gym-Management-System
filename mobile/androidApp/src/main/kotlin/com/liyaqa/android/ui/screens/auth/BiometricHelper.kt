package com.liyaqa.android.ui.screens.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper class for biometric authentication
 * Provides methods to check biometric availability and perform authentication
 */
class BiometricHelper(private val context: Context) {

    /**
     * Check if biometric authentication is available on the device
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Get the status message for biometric authentication
     */
    fun getBiometricStatus(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                BiometricStatus.Available

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricStatus.NotAvailable("Biometric hardware not available")

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricStatus.NotAvailable("Biometric hardware currently unavailable")

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricStatus.NotEnrolled("No biometric credentials enrolled")

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                BiometricStatus.NotAvailable("Security update required")

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                BiometricStatus.NotAvailable("Biometric authentication not supported")

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                BiometricStatus.NotAvailable("Biometric status unknown")

            else ->
                BiometricStatus.NotAvailable("Biometric authentication not available")
        }
    }

    /**
     * Show biometric authentication prompt
     * @param activity The FragmentActivity to show the prompt from
     * @param title The title to display in the prompt
     * @param subtitle The subtitle to display in the prompt
     * @param description The description to display in the prompt
     * @param negativeButtonText The text for the negative button (fallback)
     * @param onSuccess Callback when authentication succeeds
     * @param onError Callback when authentication fails
     * @param onFallback Callback when user chooses fallback (negative button)
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Biometric Authentication",
        subtitle: String = "Login using your biometric credential",
        description: String = "Place your finger on the sensor or look at the camera",
        negativeButtonText: String = "Use password",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFallback: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            // User clicked the negative button (fallback to password)
                            onFallback()
                        }
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            onError("Authentication canceled by user")
                        }
                        else -> {
                            onError(errString.toString())
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // This is called when the biometric is valid but not recognized
                    // Don't call onError here as the prompt stays open for retry
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

/**
 * Sealed class representing biometric authentication status
 */
sealed class BiometricStatus {
    /**
     * Biometric authentication is available
     */
    data object Available : BiometricStatus()

    /**
     * Biometric authentication is not available
     */
    data class NotAvailable(val reason: String) : BiometricStatus()

    /**
     * Biometric hardware is available but no credentials are enrolled
     */
    data class NotEnrolled(val reason: String) : BiometricStatus()
}

/**
 * Biometric preference storage keys
 */
object BiometricPreferences {
    const val PREFS_NAME = "biometric_prefs"
    const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    const val KEY_SAVED_EMAIL = "saved_email"
}
