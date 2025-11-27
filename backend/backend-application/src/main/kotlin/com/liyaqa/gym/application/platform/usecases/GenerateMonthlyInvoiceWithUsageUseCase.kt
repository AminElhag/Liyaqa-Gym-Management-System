package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.domain.entities.tenant.InvoiceStatus
import com.liyaqa.gym.domain.entities.tenant.PlatformInvoice
import com.liyaqa.gym.domain.exceptions.NoActiveSubscriptionException
import com.liyaqa.gym.domain.repositories.PlatformInvoiceRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.domain.valueobjects.InvoiceLineItem
import com.liyaqa.gym.domain.valueobjects.Money
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

/**
 * Use case for generating monthly invoices with usage-based charges.
 * Combines subscription fees with usage charges for comprehensive billing.
 */
@Service
class GenerateMonthlyInvoiceWithUsageUseCase(
    private val calculateUsageChargesUseCase: CalculateUsageChargesUseCase,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val invoiceRepository: PlatformInvoiceRepository
) {

    /**
     * Generate a monthly invoice including both subscription and usage charges.
     *
     * @param tenantId The tenant to bill
     * @return Result containing the generated invoice
     */
    suspend fun execute(tenantId: UUID): Result<PlatformInvoice> {
        return try {
            // Find active subscription
            val subscriptionOpt = subscriptionRepository.findActiveByTenant(tenantId).getOrThrow()
            if (!subscriptionOpt.isPresent) {
                return Result.failure(NoActiveSubscriptionException(tenantId))
            }
            val subscription = subscriptionOpt.get()

            // Calculate usage charges for the previous month
            val period = YearMonth.now().minusMonths(1)
            val usageCharges = calculateUsageChargesUseCase.execute(tenantId, period).getOrThrow()

            // Build line items
            val lineItems = mutableListOf<InvoiceLineItem>()

            // Add subscription fee
            lineItems.add(
                InvoiceLineItem.createSingle(
                    description = "${subscription.plan.displayName} Subscription - Monthly",
                    descriptionArabic = "اشتراك ${subscription.plan.displayName} - شهري",
                    price = subscription.amount
                )
            )

            // Add usage charges if any
            val nonZeroCharges = usageCharges.getNonZeroCharges()

            // Add SMS charges if any
            if (nonZeroCharges.containsKey("sms") && usageCharges.smsCharges.isPositive()) {
                lineItems.add(
                    InvoiceLineItem.create(
                        description = "SMS Messages - ${formatMonth(period)}",
                        descriptionArabic = "رسائل SMS - ${formatMonthArabic(period)}",
                        quantity = usageCharges.smsCount,
                        unitPrice = calculateUnitPrice(usageCharges.smsCharges, usageCharges.smsCount)
                    )
                )
            }

            // Add email charges if any
            if (nonZeroCharges.containsKey("email") && usageCharges.emailCharges.isPositive()) {
                lineItems.add(
                    InvoiceLineItem.create(
                        description = "Email Messages - ${formatMonth(period)}",
                        descriptionArabic = "رسائل البريد الإلكتروني - ${formatMonthArabic(period)}",
                        quantity = usageCharges.emailCount,
                        unitPrice = calculateUnitPrice(usageCharges.emailCharges, usageCharges.emailCount)
                    )
                )
            }

            // Add API charges if any
            if (nonZeroCharges.containsKey("api") && usageCharges.apiCharges.isPositive()) {
                lineItems.add(
                    InvoiceLineItem.create(
                        description = "API Calls - ${formatMonth(period)}",
                        descriptionArabic = "استدعاءات API - ${formatMonthArabic(period)}",
                        quantity = usageCharges.apiCallsCount,
                        unitPrice = calculateUnitPrice(usageCharges.apiCharges, usageCharges.apiCallsCount)
                    )
                )
            }

            // Add storage charges if any
            if (nonZeroCharges.containsKey("storage") && usageCharges.storageCharges.isPositive()) {
                lineItems.add(
                    InvoiceLineItem.create(
                        description = "Storage (GB) - ${formatMonth(period)}",
                        descriptionArabic = "التخزين (جيجابايت) - ${formatMonthArabic(period)}",
                        quantity = usageCharges.storageGB,
                        unitPrice = calculateUnitPrice(usageCharges.storageCharges, usageCharges.storageGB)
                    )
                )
            }

            // Calculate total amounts
            val subtotal = lineItems.fold(Money.zero("SAR")) { acc, item ->
                acc + item.totalAmount
            }
            val vatRate = 0.15 // 15% VAT for Saudi Arabia
            val vatAmount = subtotal * BigDecimal.valueOf(vatRate)
            val totalAmount = subtotal + vatAmount

            // Create invoice
            val invoice = PlatformInvoice(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                invoiceNumber = PlatformInvoice.generateInvoiceNumber(),
                amount = subtotal,
                vatAmount = vatAmount,
                totalAmount = totalAmount,
                status = InvoiceStatus.PENDING,
                dueDate = LocalDate.now().plusDays(7),
                issueDate = LocalDate.now(),
                paidAt = null,
                items = lineItems,
                zatcaClearanceId = null,
                qrCodeData = null,
                notes = "Monthly subscription and usage charges for period: ${period}",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            // Save and return invoice
            invoiceRepository.save(invoice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculate unit price from total charges and quantity.
     */
    private fun calculateUnitPrice(totalCharges: Money, quantity: Int): Money {
        return if (quantity > 0) {
            totalCharges / BigDecimal.valueOf(quantity.toLong())
        } else {
            Money.zero(totalCharges.currency.currencyCode)
        }
    }

    /**
     * Format month for English description.
     */
    private fun formatMonth(period: YearMonth): String {
        return period.month.toString().lowercase().replaceFirstChar { it.uppercase() }
    }

    /**
     * Format month for Arabic description.
     * Basic mapping - could be enhanced with proper Arabic month names.
     */
    private fun formatMonthArabic(period: YearMonth): String {
        return when (period.monthValue) {
            1 -> "يناير"
            2 -> "فبراير"
            3 -> "مارس"
            4 -> "أبريل"
            5 -> "مايو"
            6 -> "يونيو"
            7 -> "يوليو"
            8 -> "أغسطس"
            9 -> "سبتمبر"
            10 -> "أكتوبر"
            11 -> "نوفمبر"
            12 -> "ديسمبر"
            else -> period.month.toString()
        }
    }
}
