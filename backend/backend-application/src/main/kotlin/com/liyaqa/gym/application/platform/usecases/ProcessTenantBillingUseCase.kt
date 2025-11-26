package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.domain.entities.tenant.InvoiceStatus
import com.liyaqa.gym.domain.entities.tenant.PlatformInvoice
import com.liyaqa.gym.domain.exceptions.NoActiveSubscriptionException
import com.liyaqa.gym.domain.repositories.PlatformInvoiceRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.domain.services.EventPublisher
import com.liyaqa.gym.domain.services.PaymentGateway
import com.liyaqa.gym.domain.services.TenantPaymentProcessedEvent
import com.liyaqa.gym.domain.services.ZATCAService
import com.liyaqa.gym.domain.valueobjects.InvoiceLineItem
import com.liyaqa.gym.domain.valueobjects.Money
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Use case for processing tenant billing.
 * Generates invoice, submits to ZATCA, and processes payment if payment method is on file.
 */
@Service
class ProcessTenantBillingUseCase(
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val invoiceRepository: PlatformInvoiceRepository,
    private val paymentGateway: PaymentGateway,
    private val zatcaService: ZATCAService,
    private val eventPublisher: EventPublisher
) {

    suspend fun execute(tenantId: UUID): Result<PlatformInvoice> {
        return try {
            // Find active subscription
            val subscriptionOpt = subscriptionRepository.findActiveByTenant(tenantId).getOrThrow()
            if (!subscriptionOpt.isPresent) {
                return Result.failure(NoActiveSubscriptionException(tenantId))
            }

            val subscription = subscriptionOpt.get()

            // Calculate invoice amount
            val amount = subscription.amount
            val vatRate = 0.15 // 15% VAT for Saudi Arabia
            val vatAmount = amount * vatRate.toBigDecimal()
            val totalAmount = amount + vatAmount

            // Create invoice line items
            val items = listOf(
                InvoiceLineItem.createSingle(
                    description = "${subscription.plan.displayName} Subscription - ${subscription.billingCycle}",
                    descriptionArabic = null,
                    price = amount
                )
            )

            // Create invoice
            var invoice = PlatformInvoice(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                invoiceNumber = PlatformInvoice.generateInvoiceNumber(),
                amount = amount,
                vatAmount = vatAmount,
                totalAmount = totalAmount,
                status = InvoiceStatus.PENDING,
                dueDate = LocalDate.now().plusDays(7),
                issueDate = LocalDate.now(),
                paidAt = null,
                items = items,
                zatcaClearanceId = null,
                qrCodeData = null,
                notes = "Subscription billing for ${subscription.plan.displayName}",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            invoiceRepository.save(invoice).getOrThrow()

            // Submit to ZATCA for clearance
            val clearanceResult = zatcaService.submitInvoice(invoice)
            if (clearanceResult.isSuccess && clearanceResult.clearanceId != null && clearanceResult.qrCode != null) {
                invoice = invoice.addZatcaClearance(clearanceResult.clearanceId, clearanceResult.qrCode)
                invoiceRepository.save(invoice).getOrThrow()
            }

            // Attempt payment if payment method on file
            if (subscription.paymentMethod != null) {
                val paymentResult = paymentGateway.processPayment(
                    amount = totalAmount,
                    paymentMethod = subscription.paymentMethod,
                    metadata = mapOf(
                        "invoiceId" to invoice.id.toString(),
                        "tenantId" to tenantId.toString(),
                        "subscriptionId" to subscription.id.toString()
                    )
                )

                if (paymentResult.success && paymentResult.transactionId != null) {
                    // Mark invoice as paid
                    invoice = invoice.markAsPaid()
                    invoiceRepository.save(invoice).getOrThrow()

                    // Update subscription next billing date
                    val updatedSubscription = updateNextBillingDate(subscription)
                    subscriptionRepository.save(updatedSubscription).getOrThrow()

                    // Publish payment event
                    eventPublisher.publish(
                        TenantPaymentProcessedEvent(
                            tenantId = tenantId,
                            invoiceId = invoice.id,
                            amount = totalAmount.amount.toDouble(),
                            currency = totalAmount.currency.currencyCode,
                            occurredAt = Instant.now()
                        )
                    )
                }
            }

            Result.success(invoice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun updateNextBillingDate(subscription: com.liyaqa.gym.domain.entities.tenant.TenantSubscription):
            com.liyaqa.gym.domain.entities.tenant.TenantSubscription {
        val nextBillingDate = when (subscription.billingCycle) {
            com.liyaqa.gym.domain.entities.tenant.BillingCycle.MONTHLY ->
                subscription.nextBillingDate.plusMonths(1)
            com.liyaqa.gym.domain.entities.tenant.BillingCycle.QUARTERLY ->
                subscription.nextBillingDate.plusMonths(3)
            com.liyaqa.gym.domain.entities.tenant.BillingCycle.ANNUAL ->
                subscription.nextBillingDate.plusYears(1)
        }

        return subscription.copy(
            nextBillingDate = nextBillingDate,
            updatedAt = Instant.now()
        )
    }
}
