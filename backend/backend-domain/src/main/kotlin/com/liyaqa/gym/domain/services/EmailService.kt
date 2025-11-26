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

    /**
     * Send payment failure notice to tenant.
     */
    suspend fun sendPaymentFailureNotice(
        tenant: Tenant,
        reason: String,
        retryDate: LocalDate
    ): Result<Unit>

    /**
     * Send urgent payment notice to tenant.
     */
    suspend fun sendUrgentPaymentNotice(
        tenant: Tenant,
        reason: String,
        retryDate: LocalDate
    ): Result<Unit>

    /**
     * Send account suspension notice to tenant.
     */
    suspend fun sendAccountSuspensionNotice(
        tenant: Tenant,
        reason: String
    ): Result<Unit>

    /**
     * Send trial converted to paid notice to tenant.
     */
    suspend fun sendTrialConvertedNotice(
        tenant: Tenant
    ): Result<Unit>

    /**
     * Send trial expired with payment failed notice to tenant.
     */
    suspend fun sendTrialExpiredPaymentFailedNotice(
        tenant: Tenant
    ): Result<Unit>

    /**
     * Send trial expired without payment method notice to tenant.
     */
    suspend fun sendTrialExpiredNoPaymentMethodNotice(
        tenant: Tenant
    ): Result<Unit>

    /**
     * Send payment reminder email to tenant.
     */
    suspend fun sendPaymentReminderEmail(
        tenant: Tenant,
        upcomingBillingDate: LocalDate,
        amount: Double
    ): Result<Unit>
}
