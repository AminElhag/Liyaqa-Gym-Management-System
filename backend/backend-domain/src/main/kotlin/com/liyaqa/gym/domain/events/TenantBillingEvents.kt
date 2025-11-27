package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event published when a tenant payment has failed.
 */
data class TenantPaymentFailedEvent(
    val tenantId: UUID,
    val failureCount: Int,
    val reason: String,
    override val occurredAt: Instant
) : DomainEvent()

/**
 * Event published when a trial subscription has expired.
 */
data class TenantTrialExpiredEvent(
    val tenantId: UUID,
    val hadPaymentMethod: Boolean,
    val wasConverted: Boolean,
    override val occurredAt: Instant
) : DomainEvent()

/**
 * Event published when an invoice is sent to a tenant.
 */
data class TenantInvoiceSentEvent(
    val tenantId: UUID,
    val invoiceId: UUID,
    val invoiceNumber: String,
    val amount: Double,
    override val occurredAt: Instant
) : DomainEvent()

/**
 * Event published when a payment reminder is sent.
 */
data class TenantPaymentReminderSentEvent(
    val tenantId: UUID,
    val upcomingBillingDate: String,
    val amount: Double,
    override val occurredAt: Instant
) : DomainEvent()

/**
 * Event published when automated billing is processed.
 */
data class TenantBillingProcessedEvent(
    val tenantId: UUID,
    val invoiceId: UUID,
    val amount: Double,
    val successful: Boolean,
    override val occurredAt: Instant
) : DomainEvent()
