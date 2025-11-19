package com.liyaqa.gym.application.financial.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * DTO for VAT report data.
 *
 * Contains aggregated VAT information for a specific period,
 * formatted for ZATCA portal submission.
 */
data class VATReportDTO(
    val reportId: UUID,
    val branchId: UUID?,
    val branchName: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val currency: String,

    // Output VAT (sales)
    val totalSales: BigDecimal,
    val totalOutputVAT: BigDecimal,
    val standardRatedSales: BigDecimal,
    val zeroRatedSales: BigDecimal,
    val exemptSales: BigDecimal,

    // Input VAT (purchases/expenses)
    val totalPurchases: BigDecimal,
    val totalInputVAT: BigDecimal,

    // Net VAT
    val netVAT: BigDecimal, // Output VAT - Input VAT

    // Invoice statistics
    val totalInvoiceCount: Int,
    val clearedInvoiceCount: Int,
    val pendingInvoiceCount: Int,
    val failedInvoiceCount: Int,

    // Timestamps
    val generatedAt: java.time.Instant
) {
    /**
     * Formats the report for ZATCA portal submission.
     */
    fun toZATCAFormat(): String {
        return """
            VAT RETURN REPORT
            Period: $startDate to $endDate
            ${branchName?.let { "Branch: $it" } ?: "All Branches"}

            OUTPUT VAT (SALES):
            - Standard Rated Sales (15%): $standardRatedSales $currency
            - Zero Rated Sales (0%): $zeroRatedSales $currency
            - Exempt Sales: $exemptSales $currency
            - Total Sales: $totalSales $currency
            - Total Output VAT: $totalOutputVAT $currency

            INPUT VAT (PURCHASES):
            - Total Purchases: $totalPurchases $currency
            - Total Input VAT: $totalInputVAT $currency

            NET VAT:
            - Net VAT Payable: $netVAT $currency

            INVOICES:
            - Total Invoices: $totalInvoiceCount
            - ZATCA Cleared: $clearedInvoiceCount
            - ZATCA Pending: $pendingInvoiceCount
            - ZATCA Failed: $failedInvoiceCount

            Generated: $generatedAt
        """.trimIndent()
    }
}
