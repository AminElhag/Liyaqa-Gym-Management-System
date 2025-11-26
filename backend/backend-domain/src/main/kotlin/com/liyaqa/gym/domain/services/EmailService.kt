package com.liyaqa.gym.domain.services

import com.liyaqa.gym.domain.entities.tenant.Tenant
import java.time.LocalDate

/**
 * Service interface for sending emails.
 */
interface EmailService {

    /**
     * Send welcome email to new tenant with login credentials.
     */
    suspend fun sendTenantWelcomeEmail(
        to: String,
        tenantName: String,
        username: String,
        temporaryPassword: String,
        loginUrl: String,
        trialEndsAt: LocalDate?
    ): Result<Unit>

    /**
     * Send suspension notice to tenant.
     */
    suspend fun sendTenantSuspensionNotice(
        tenant: Tenant,
        reason: String
    ): Result<Unit>

    /**
     * Send reactivation confirmation to tenant.
     */
    suspend fun sendTenantReactivationConfirmation(
        tenant: Tenant
    ): Result<Unit>

    /**
     * Send plan upgrade confirmation to tenant.
     */
    suspend fun sendPlanUpgradeConfirmation(
        tenant: Tenant,
        newPlan: String,
        effectiveDate: LocalDate
    ): Result<Unit>

    /**
     * Send plan downgrade notice to tenant.
     */
    suspend fun sendPlanDowngradeNotice(
        tenant: Tenant,
        newPlan: String,
        effectiveDate: LocalDate
    ): Result<Unit>

    /**
     * Send subscription cancellation confirmation.
     */
    suspend fun sendSubscriptionCancellationConfirmation(
        tenant: Tenant,
        endDate: LocalDate
    ): Result<Unit>

    /**
     * Send invoice to tenant.
     */
    suspend fun sendInvoice(
        tenantEmail: String,
        invoiceId: String,
        invoiceNumber: String,
        amount: Double,
        dueDate: LocalDate
    ): Result<Unit>
}
