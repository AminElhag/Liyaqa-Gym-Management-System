package com.liyaqa.android.ui.screens.checkin

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Utility object for generating QR codes
 * Uses ZXing library to create QR code bitmaps
 */
object QRCodeGenerator {

    /**
     * Generate a QR code bitmap from the given data
     *
     * @param data The data to encode in the QR code
     * @param size The size of the QR code in pixels (both width and height)
     * @param foregroundColor The color of the QR code modules (default: black)
     * @param backgroundColor The background color (default: white)
     * @return Bitmap containing the QR code, or null if generation failed
     */
    fun generateQRCode(
        data: String,
        size: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        return try {
            // Configure QR code generation
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H) // High error correction
                put(EncodeHintType.MARGIN, 1) // Minimal margin
            }

            // Generate QR code bit matrix
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)

            // Convert bit matrix to bitmap
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) {
                        foregroundColor
                    } else {
                        backgroundColor
                    }
                }
            }

            // Create and return the bitmap
            Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate a QR code from QRCodeData model
     *
     * @param qrCodeData The QR code data model to encode
     * @param size The size of the QR code in pixels
     * @return Bitmap containing the QR code, or null if generation failed
     */
    fun generateQRCode(
        qrCodeData: QRCodeData,
        size: Int = 512
    ): Bitmap? {
        return generateQRCode(
            data = qrCodeData.toEncodedString(),
            size = size
        )
    }

    /**
     * Generate a colored QR code (e.g., for branding)
     *
     * @param data The data to encode
     * @param size The size of the QR code
     * @param primaryColor The primary color (for QR modules)
     * @param secondaryColor The secondary color (for background)
     * @return Bitmap containing the colored QR code
     */
    fun generateColoredQRCode(
        data: String,
        size: Int = 512,
        primaryColor: Int,
        secondaryColor: Int
    ): Bitmap? {
        return generateQRCode(
            data = data,
            size = size,
            foregroundColor = primaryColor,
            backgroundColor = secondaryColor
        )
    }
}
