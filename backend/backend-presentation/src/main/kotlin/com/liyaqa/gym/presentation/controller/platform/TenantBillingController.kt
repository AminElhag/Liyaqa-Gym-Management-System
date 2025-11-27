package com.liyaqa.gym.presentation.controller.platform

import com.liyaqa.gym.application.platform.usecases.ProcessTenantBillingUseCase
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.presentation.dto.platform.InvoiceResponse
import com.liyaqa.gym.presentation.dto.platform.UpdatePaymentMethodRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Tenant Billing Controller
 * Handles platform admin operations for tenant billing and invoicing
 */
@RestController
@RequestMapping("/api/v1/platform/tenants/{tenantId}/billing")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@Tag(name = "Platform Tenant Billing", description = "Platform administrator tenant billing endpoints")
class TenantBillingController(
    private val processBillingUseCase: ProcessTenantBillingUseCase,
    private val subscriptionRepository: TenantSubscriptionRepository
) {

    private val logger = LoggerFactory.getLogger(TenantBillingController::class.java)

    /**
     * Get tenant invoices
     */
    @GetMapping("/invoices")
    @Operation(summary = "Get tenant invoices", description = "Get all invoices for a specific tenant")
    fun getTenantInvoices(
        @Parameter(description = "Tenant ID")
        @PathVariable tenantId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<InvoiceResponse>> {
        logger.info("Getting invoices for tenant: $tenantId")

        // TODO: Implement GetTenantInvoicesUseCase when available
        // For now, return empty page with mock data
        val mockInvoices = listOf<InvoiceResponse>()
        val page = PageImpl(mockInvoices, pageable, mockInvoices.size.toLong())

        return ResponseEntity.ok(page)
    }

    /**
     * Process billing for a tenant
     */
    @PostMapping("/process")
    @Operation(summary = "Process billing", description = "Manually trigger billing process for a tenant")
    fun processBilling(
        @Parameter(description = "Tenant ID")
        @PathVariable tenantId: UUID
    ): ResponseEntity<InvoiceResponse> {
        logger.info("Processing billing for tenant: $tenantId")

        val invoice = runBlocking {
            processBillingUseCase.execute(tenantId).getOrThrow()
        }

        // Convert to response
        val response = InvoiceResponse(
            id = invoice.id,
            tenantId = invoice.tenantId,
            invoiceNumber = invoice.invoiceNumber,
            amount = invoice.totalAmount.amount.toDouble(),
            currency = "SAR", // Saudi Riyal - default currency
            status = invoice.status.name,
            dueDate = invoice.dueDate,
            paidAt = invoice.paidAt,
            createdAt = invoice.createdAt
        )

        logger.info("Billing processed successfully for tenant: $tenantId")
        return ResponseEntity.ok(response)
    }

    /**
     * Update tenant payment method
     */
    @PutMapping("/payment-method")
    @Operation(summary = "Update payment method", description = "Update payment method for a tenant")
    fun updatePaymentMethod(
        @Parameter(description = "Tenant ID")
        @PathVariable tenantId: UUID,
        @RequestBody @Valid request: UpdatePaymentMethodRequest
    ): ResponseEntity<Unit> {
        logger.info("Updating payment method for tenant: $tenantId")

        // TODO: Implement UpdateTenantPaymentMethodUseCase when available
        // For now, just update the subscription
        val subscriptionOpt = runBlocking {
            subscriptionRepository.findActiveByTenant(tenantId).getOrNull()
        }

        if (subscriptionOpt == null || !subscriptionOpt.isPresent) {
            throw IllegalArgumentException("No active subscription found for tenant: $tenantId")
        }

        val subscription = subscriptionOpt.get()

        // Convert payment method string to enum (or keep null if not valid)
        val paymentMethod = try {
            com.liyaqa.gym.domain.entities.PaymentMethod.valueOf(request.paymentMethodId.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }

        val updatedSubscription = subscription.copy(
            paymentMethod = paymentMethod,
            updatedAt = Instant.now()
        )

        runBlocking {
            subscriptionRepository.save(updatedSubscription)
        }

        logger.info("Payment method updated successfully for tenant: $tenantId")
        return ResponseEntity.ok().build()
    }

    /**
     * Get tenant billing summary
     */
    @GetMapping("/summary")
    @Operation(summary = "Get billing summary", description = "Get billing summary for a tenant")
    fun getBillingSummary(
        @Parameter(description = "Tenant ID")
        @PathVariable tenantId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting billing summary for tenant: $tenantId")

        val subscriptionOpt = runBlocking {
            subscriptionRepository.findActiveByTenant(tenantId).getOrNull()
        }

        val subscription = subscriptionOpt?.orElse(null)

        val summary = mapOf(
            "tenantId" to tenantId,
            "currentPlan" to (subscription?.plan?.displayName ?: "N/A"),
            "billingCycle" to (subscription?.billingCycle?.name ?: "N/A"),
            "nextBillingDate" to (subscription?.nextBillingDate ?: LocalDate.now()),
            "amount" to (subscription?.amount?.amount?.toDouble() ?: 0.0),
            "currency" to "SAR", // Saudi Riyal - default currency
            "status" to (subscription?.status?.name ?: "UNKNOWN"),
            "autoRenew" to (subscription?.autoRenew ?: false),
            "paymentMethod" to (subscription?.paymentMethod ?: "Not set")
        )

        return ResponseEntity.ok(summary)
    }
}
