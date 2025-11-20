package com.liyaqa.android.ui.screens.checkin

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.util.Log
import java.nio.charset.Charset

/**
 * Helper class for NFC-based check-in
 * Provides utilities for reading and writing NFC tags
 */
class NFCHelper(private val context: Context) {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFilters: Array<IntentFilter>? = null

    companion object {
        private const val TAG = "NFCHelper"
        const val MIME_TYPE = "application/vnd.liyaqa.checkin"
    }

    init {
        initializeNFC()
    }

    /**
     * Initialize NFC adapter and intents
     */
    private fun initializeNFC() {
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(context)

            if (nfcAdapter != null) {
                val intent = Intent(context, context.javaClass).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)

                // Setup intent filters for NFC discovery
                val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                    try {
                        addDataType(MIME_TYPE)
                    } catch (e: IntentFilter.MalformedMimeTypeException) {
                        Log.e(TAG, "Malformed MIME type", e)
                    }
                }

                intentFilters = arrayOf(ndefDetected)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize NFC", e)
        }
    }

    /**
     * Check if NFC is available on this device
     */
    fun isNFCAvailable(): Boolean {
        return nfcAdapter != null
    }

    /**
     * Check if NFC is enabled
     */
    fun isNFCEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }

    /**
     * Enable foreground dispatch to receive NFC events
     * Call this in onResume()
     */
    fun enableForegroundDispatch(activity: Activity) {
        try {
            if (nfcAdapter?.isEnabled == true) {
                nfcAdapter?.enableForegroundDispatch(
                    activity,
                    pendingIntent,
                    intentFilters,
                    null
                )
                Log.d(TAG, "NFC foreground dispatch enabled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable foreground dispatch", e)
        }
    }

    /**
     * Disable foreground dispatch
     * Call this in onPause()
     */
    fun disableForegroundDispatch(activity: Activity) {
        try {
            nfcAdapter?.disableForegroundDispatch(activity)
            Log.d(TAG, "NFC foreground dispatch disabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable foreground dispatch", e)
        }
    }

    /**
     * Write check-in data to NFC tag
     * This can be used to write member information to a physical NFC card
     *
     * @param tag The NFC tag to write to
     * @param qrCodeData The QR code data to write
     * @return True if write was successful, false otherwise
     */
    fun writeCheckInDataToTag(tag: Tag, qrCodeData: QRCodeData): Boolean {
        return try {
            val ndef = Ndef.get(tag) ?: return false

            ndef.connect()

            val message = createNdefMessage(qrCodeData)

            if (message.toByteArray().size > ndef.maxSize) {
                Log.e(TAG, "Message too large for tag")
                ndef.close()
                return false
            }

            ndef.writeNdefMessage(message)
            ndef.close()

            Log.d(TAG, "Successfully wrote check-in data to NFC tag")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write NFC tag", e)
            false
        }
    }

    /**
     * Read check-in data from NFC tag
     *
     * @param intent The intent containing the NFC tag
     * @return QRCodeData if successfully read, null otherwise
     */
    fun readCheckInDataFromTag(intent: Intent): QRCodeData? {
        return try {
            val action = intent.action

            if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
                val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

                if (rawMessages != null && rawMessages.isNotEmpty()) {
                    val ndefMessage = rawMessages[0] as NdefMessage
                    val records = ndefMessage.records

                    if (records.isNotEmpty()) {
                        val payload = records[0].payload
                        val text = String(payload, Charset.forName("UTF-8"))

                        // Parse the check-in data
                        QRCodeData.fromEncodedString(text)
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read NFC tag", e)
            null
        }
    }

    /**
     * Create NDEF message from QR code data
     */
    private fun createNdefMessage(qrCodeData: QRCodeData): NdefMessage {
        val encodedData = qrCodeData.toEncodedString()
        val payload = encodedData.toByteArray(Charset.forName("UTF-8"))

        val mimeRecord = NdefRecord.createMime(MIME_TYPE, payload)

        return NdefMessage(arrayOf(mimeRecord))
    }

    /**
     * Get a user-friendly message about NFC status
     */
    fun getNFCStatusMessage(): String {
        return when {
            !isNFCAvailable() -> "NFC is not available on this device"
            !isNFCEnabled() -> "NFC is disabled. Please enable it in Settings"
            else -> "NFC is ready"
        }
    }

    /**
     * Open NFC settings
     */
    fun openNFCSettings(activity: Activity) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
            } else {
                Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open NFC settings", e)
        }
    }
}

/**
 * Extension function to handle NFC intent in an activity
 */
fun Activity.handleNFCIntent(
    intent: Intent,
    nfcHelper: NFCHelper,
    onCheckInDataRead: (QRCodeData) -> Unit
) {
    if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
        val checkInData = nfcHelper.readCheckInDataFromTag(intent)
        if (checkInData != null) {
            onCheckInDataRead(checkInData)
        }
    }
}
