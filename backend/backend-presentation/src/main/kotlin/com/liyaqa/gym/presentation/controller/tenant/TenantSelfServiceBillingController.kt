package com.liyaqa.gym.presentation.controller.tenant

import com.liyaqa.gym.domain.entities.tenant.TenantUser
import com.liyaqa.gym.domain.repositories.PlatformInvoiceRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.presentation.dto.platform.InvoiceResponse
import com.liyaqa.gym.presentation.dto.platform.SubscriptionDetailsResponse
import com.liyaqa.gym.presentation.dto.platform.UpdatePaymentMethodRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Tenant Self-Service Billing Controller
 * Allows tenants to manage their own billing, invoices, and payment methods
 */
@RestController
@RequestMapping("/api/v1/tenant/billing")
@PreAuthorize("hasAnyRole('TENANT_OWNER', 'TENANT_ADMIN')")
@Tag(name = "Tenant Billing", description = "Tenant self-service billing endpoints")
class TenantSelfServiceBillingController(
    private val invoiceRepository: PlatformInvoiceRepository,
    private val subscriptionRepository: TenantSubscriptionRepository
) {

    private val logger = LoggerFactory.getLogger(TenantSelfServiceBillingController::class.java)

    /**
     * Get tenant's invoices
     */
    @GetMapping("/invoices")
    @Operation(summary = "Get invoices", description = "Get all invoices for the authenticated tenant")
    fun getInvoices(
        @AuthenticationPrincipal user: TenantUser,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<InvoiceResponse>> {
        logger.info("Getting invoices for tenant: ${user.tenantId}")

        val invoices = runBlocking {
            invoiceRepository.findByTenant(user.tenantId, pageable)
                .getOrThrow()
                .map { invoice ->
                    InvoiceResponse(
                        id = invoice.id,
                        tenantId = invoice.tenantId,
                        invoiceNumber = invoice.invoiceNumber,
                        amount = invoice.amount.amount.toDouble(),
                        currency = invoice.amount.currency.currencyCode,
                        status = invoice.status.name,
                        dueDate = invoice.dueDate,
                        paidAt = invoice.paidAt,
                        createdAt = invoice.createdAt
                    )
                }
        }

        return ResponseEntity.ok(invoices)
    }

    /**
     * Download invoice PDF
     */
    @GetMapping("/invoices/{invoiceId}/download")
    @Operation(summary = "Download invoice", description = "Download invoice PDF")
    fun downloadInvoice(
        @PathVariable invoiceId: UUID,
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<Resource> {
        logger.info("Downloading invoice $invoiceId for tenant ${user.tenantId}")

        // Find invoice
        val invoice = runBlocking {
            val invoiceOpt = invoiceRepository.findById(invoiceId).getOrThrow()
            if (!invoiceOpt.isPresent) {
                throw IllegalArgumentException("Invoice not found: $invoiceId")
            }
            val inv = invoiceOpt.get()

            // Verify that invoice belongs to the tenant
            if (inv.tenantId != user.tenantId) {
                throw SecurityException("Access denied to invoice $invoiceId")
            }

            inv
        }

        // For now, return a placeholder PDF
        // TODO: Implement actual PDF generation using PdfService
        val pdfContent = "Invoice ${invoice.invoiceNumber}\nTotal: ${invoice.totalAmount}".toByteArray()
        val resource = ByteArrayResource(pdfContent)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-${invoice.invoiceNumber}.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfContent.size.toLong())
            .body(resource)
    }

    /**
     * Update payment method
     */
    @PostMapping("/payment-method")
    @Operation(summary = "Update payment method", description = "Update payment method for subscription")
    fun updatePaymentMethod(
        @RequestBody @Valid request: UpdatePaymentMethodRequest,
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<Unit> {
        logger.info("Updating payment method for tenant: ${user.tenantId}")

        runBlocking {
            // Find active subscription
            val subscriptionOpt = subscriptionRepository.findActiveByTenant(user.tenantId).getOrThrow()
            if (!subscriptionOpt.isPresent) {
                throw IllegalArgumentException("No active subscription found for tenant: ${user.tenantId}")
            }
            val subscription = subscriptionOpt.get()

            // TODO: Validate payment method with payment gateway
            // For now, just update the subscription
            val updatedSubscription = subscription.updatePaymentMethod(
                com.liyaqa.gym.domain.entities.PaymentMethod.CREDIT_CARD // Placeholder
            )
            subscriptionRepository.save(updatedSubscription).getOrThrow()
        }

        return ResponseEntity.ok().build()
    }

    /**
     * Get subscription details
     */
    @GetMapping("/subscription")
    @Operation(summary = "Get subscription details", description = "Get current subscription details")
    fun getSubscriptionDetails(
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<SubscriptionDetailsResponse> {
        logger.info("Getting subscription details for tenant: ${user.tenantId}")

        val subscription = runBlocking {
            val subscriptionOpt = subscriptionRepository.findActiveByTenant(user.tenantId).getOrThrow()
            if (!subscriptionOpt.isPresent) {
                throw IllegalArgumentException("No active subscription found for tenant: ${user.tenantId}")
            }
            subscriptionOpt.get()
        }

        val response = SubscriptionDetailsResponse(
            id = subscription.id,
            plan = subscription.plan.displayName,
            status = subscription.status.name,
            billingCycle = subscription.billingCycle.name,
            amount = subscription.amount.amount.toDouble(),
            currency = subscription.amount.currency.currencyCode,
            nextBillingDate = subscription.nextBillingDate,
            autoRenew = subscription.autoRenew,
            trialEndsAt = subscription.trialEndsAt,
            paymentFailureCount = subscription.paymentFailureCount,
            lastPaymentFailureAt = subscription.lastPaymentFailureAt
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Get billing history summary
     */
    @GetMapping("/summary")
    @Operation(summary = "Get billing summary", description = "Get billing summary with stats")
    fun getBillingSummary(
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting billing summary for tenant: ${user.tenantId}")

        val subscription = runBlocking {
            val subscriptionOpt = subscriptionRepository.findActiveByTenant(user.tenantId).getOrThrow()
            subscriptionOpt.orElse(null)
        }

        val summary = mapOf(
            "currentPlan" to (subscription?.plan?.displayName ?: "N/A"),
            "billingCycle" to (subscription?.billingCycle?.name ?: "N/A"),
            "nextBillingDate" to (subscription?.nextBillingDate?.toString() ?: "N/A"),
            "amount" to (subscription?.amount?.amount?.toDouble() ?: 0.0),
            "currency" to (subscription?.amount?.currency?.currencyCode ?: "SAR"),
            "status" to (subscription?.status?.name ?: "UNKNOWN"),
            "autoRenew" to (subscription?.autoRenew ?: false),
            "hasPaymentMethod" to (subscription?.paymentMethod != null),
            "paymentFailureCount" to (subscription?.paymentFailureCount ?: 0)
        )

        return ResponseEntity.ok(summary)
    }
}
