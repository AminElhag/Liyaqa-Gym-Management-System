package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.TenantInvoiceSentEvent
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import com.liyaqa.gym.domain.repositories.PlatformInvoiceRepository
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.services.EmailService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

/**
 * Use case for sending invoice emails to tenants.
 */
@Service
class SendInvoiceEmailUseCase(
    private val invoiceRepository: PlatformInvoiceRepository,
    private val tenantRepository: TenantRepository,
    private val emailService: EmailService,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(SendInvoiceEmailUseCase::class.java)

    suspend fun execute(invoiceId: UUID): Result<Unit> {
        return try {
            logger.info("Sending invoice email for invoice: $invoiceId")

            // Find invoice
            val invoiceOpt = invoiceRepository.findById(invoiceId).getOrThrow()
            if (!invoiceOpt.isPresent) {
                return Result.failure(IllegalArgumentException("Invoice not found: $invoiceId"))
            }
            val invoice = invoiceOpt.get()

            // Find tenant
            val tenantOpt = tenantRepository.findById(invoice.tenantId).getOrThrow()
            if (!tenantOpt.isPresent) {
                return Result.failure(TenantNotFoundException(invoice.tenantId))
            }
            val tenant = tenantOpt.get()

            // Get billing email
            val billingEmail = tenant.contactInfo.billingContactEmail
                ?: tenant.contactInfo.primaryContactEmail

            // Send invoice email
            emailService.sendInvoice(
                tenantEmail = billingEmail,
                invoiceId = invoice.id.toString(),
                invoiceNumber = invoice.invoiceNumber,
                amount = invoice.totalAmount.amount.toDouble(),
                dueDate = invoice.dueDate
            ).getOrThrow()

            // Publish event
            eventPublisher.publish(
                TenantInvoiceSentEvent(
                    tenantId = invoice.tenantId,
                    invoiceId = invoice.id,
                    invoiceNumber = invoice.invoiceNumber,
                    amount = invoice.totalAmount.amount.toDouble(),
                    occurredAt = Instant.now()
                )
            )

            logger.info("Invoice email sent successfully for invoice: $invoiceId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to send invoice email for invoice: $invoiceId", e)
            Result.failure(e)
        }
    }
}
