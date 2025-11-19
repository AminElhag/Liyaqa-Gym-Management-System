package com.liyaqa.gym.application.financial

import com.liyaqa.gym.application.financial.dto.VATReportDTO
import com.liyaqa.gym.domain.entities.ZATCAStatus
import com.liyaqa.gym.domain.repositories.InvoiceRepository
import com.liyaqa.gym.domain.valueobjects.Money
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Use case for generating VAT reports for ZATCA submission.
 *
 * This use case handles:
 * - Aggregating all invoices in a specified period
 * - Calculating total output VAT (from sales)
 * - Calculating total input VAT (from expenses) - placeholder for future implementation
 * - Generating VAT return data
 * - Exporting in format suitable for ZATCA portal
 * - Branch-specific or organization-wide reporting
 *
 * @property invoiceRepository Repository for invoice data
 */
@Service
@Transactional(readOnly = true)
class GenerateVATReportUseCase(
    private val invoiceRepository: InvoiceRepository
) {

    private val logger = LoggerFactory.getLogger(GenerateVATReportUseCase::class.java)

    /**
     * Executes the VAT report generation use case.
     *
     * @param startDate Start date of the reporting period
     * @param endDate End date of the reporting period
     * @param branchId Optional branch ID to filter by specific branch
     * @return Result containing the VAT report DTO or error
     */
    fun execute(
        startDate: LocalDate,
        endDate: LocalDate,
        branchId: UUID? = null
    ): Result<VATReportDTO> {
        return runCatching {
            logger.info("Generating VAT report for period: $startDate to $endDate, branch: ${branchId ?: "All"}")

            // 1. Validate date range
            validateDateRange(startDate, endDate)

            // 2. Retrieve all invoices in the period
            val invoices = if (branchId != null) {
                invoiceRepository.findByBranchAndDateRange(branchId, startDate, endDate)
            } else {
                invoiceRepository.findByDateRange(startDate, endDate, branchId = null)
            }.getOrElse { error ->
                logger.error("Failed to retrieve invoices for VAT report: ${error.message}", error)
                throw error
            }

            logger.info("Retrieved ${invoices.size} invoices for VAT report")

            // 3. Calculate output VAT (from sales)
            val currency = if (invoices.isNotEmpty()) {
                invoices.first().totalAmount.currency.currencyCode
            } else {
                "SAR"
            }

            val totalSales = invoices.fold(Money.zero(currency)) { acc, invoice ->
                acc + invoice.subtotal
            }

            val totalOutputVAT = invoices.fold(Money.zero(currency)) { acc, invoice ->
                acc + invoice.vat.amount
            }

            val totalAmount = invoices.fold(Money.zero(currency)) { acc, invoice ->
                acc + invoice.totalAmount
            }

            // 4. Calculate standard rated, zero rated, and exempt sales
            // For now, all sales are standard rated (15% VAT)
            // In production, you would categorize based on invoice/product tax categories
            val standardRatedSales = totalSales
            val zeroRatedSales = Money.zero(currency)
            val exemptSales = Money.zero(currency)

            // 5. Calculate input VAT (from purchases/expenses)
            // Note: This is a placeholder. In production, you would have:
            // - Expense tracking
            // - Purchase invoices
            // - Input VAT calculations
            val totalPurchases = Money.zero(currency)
            val totalInputVAT = Money.zero(currency)

            // 6. Calculate net VAT (Output VAT - Input VAT)
            val netVAT = totalOutputVAT - totalInputVAT

            // 7. Count invoices by ZATCA status
            val clearedCount = invoices.count { it.zatcaStatus == ZATCAStatus.CLEARED }
            val pendingCount = invoices.count { it.zatcaStatus == ZATCAStatus.PENDING }
            val failedCount = invoices.count { it.zatcaStatus == ZATCAStatus.FAILED }

            // 8. Build VAT report DTO
            val report = VATReportDTO(
                reportId = UUID.randomUUID(),
                branchId = branchId,
                branchName = null, // Would be populated from branch entity in production
                startDate = startDate,
                endDate = endDate,
                currency = currency,
                totalSales = totalSales.amount,
                totalOutputVAT = totalOutputVAT.amount,
                standardRatedSales = standardRatedSales.amount,
                zeroRatedSales = zeroRatedSales.amount,
                exemptSales = exemptSales.amount,
                totalPurchases = totalPurchases.amount,
                totalInputVAT = totalInputVAT.amount,
                netVAT = netVAT.amount,
                totalInvoiceCount = invoices.size,
                clearedInvoiceCount = clearedCount,
                pendingInvoiceCount = pendingCount,
                failedInvoiceCount = failedCount,
                generatedAt = Instant.now()
            )

            logger.info(
                "VAT report generated - Total Sales: ${totalSales.amount}, " +
                "Output VAT: ${totalOutputVAT.amount}, Net VAT: ${netVAT.amount}"
            )

            report

        }.onFailure { error ->
            logger.error("Failed to generate VAT report: ${error.message}", error)
        }
    }

    /**
     * Validates the date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @throws IllegalArgumentException if date range is invalid
     */
    private fun validateDateRange(startDate: LocalDate, endDate: LocalDate) {
        require(!endDate.isBefore(startDate)) {
            "End date cannot be before start date"
        }

        val daysDifference = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)
        require(daysDifference <= 365) {
            "Date range cannot exceed 365 days"
        }

        logger.debug("Date range validation passed: $startDate to $endDate ($daysDifference days)")
    }
}
