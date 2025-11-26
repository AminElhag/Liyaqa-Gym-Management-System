package com.liyaqa.gym.domain.services

import com.liyaqa.gym.domain.entities.tenant.PlatformInvoice

/**
 * Service interface for Saudi Arabia ZATCA (Zakat, Tax and Customs Authority) integration.
 * Handles e-invoice submission and clearance for tax compliance.
 */
interface ZATCAService {

    /**
     * Submit an invoice to ZATCA for clearance.
     *
     * @param invoice The invoice to submit
     * @return Result containing clearance information
     */
    suspend fun submitInvoice(invoice: PlatformInvoice): ZATCAClearanceResult

    /**
     * Report an invoice to ZATCA (for simplified invoices).
     */
    suspend fun reportInvoice(invoice: PlatformInvoice): Result<Unit>

    /**
     * Generate QR code for invoice.
     */
    suspend fun generateQRCode(invoice: PlatformInvoice): Result<String>
}

/**
 * Result of ZATCA invoice clearance.
 */
data class ZATCAClearanceResult(
    val success: Boolean,
    val clearanceId: String?,
    val qrCode: String?,
    val errorMessage: String?
) {
    val isSuccess: Boolean get() = success

    companion object {
        fun success(clearanceId: String, qrCode: String) = ZATCAClearanceResult(
            success = true,
            clearanceId = clearanceId,
            qrCode = qrCode,
            errorMessage = null
        )

        fun failure(errorMessage: String) = ZATCAClearanceResult(
            success = false,
            clearanceId = null,
            qrCode = null,
            errorMessage = errorMessage
        )
    }
}
