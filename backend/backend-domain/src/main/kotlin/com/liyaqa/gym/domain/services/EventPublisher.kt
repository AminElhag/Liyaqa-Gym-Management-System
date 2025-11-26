package com.liyaqa.gym.domain.services

import java.time.Instant
import java.util.UUID

/**
 * Service interface for publishing domain events.
 */
interface EventPublisher {

    /**
     * Publish a domain event.
     */
    suspend fun publish(event: DomainEvent): Result<Unit>

    /**
     * Publish multiple domain events.
     */
    suspend fun publishAll(events: List<DomainEvent>): Result<Unit>
}

/**
 * Base interface for all domain events.
 */
interface DomainEvent {
    val occurredAt: Instant
    val eventType: String
}

/**
 * Event published when a new tenant is created.
 */
data class TenantCreatedEvent(
    val tenantId: UUID,
    val tenantName: String,
    val ownerEmail: String,
    override val occurredAt: Instant
) : DomainEvent {
    override val eventType: String = "TenantCreated"
}

/**
 * Event published when a tenant is suspended.
 */
data class TenantSuspendedEvent(
    val tenantId: UUID,
    val tenantName: String,
    val reason: String,
    override val occurredAt: Instant
) : DomainEvent {
    override val eventType: String = "TenantSuspended"
}

/**
 * Event published when a tenant is reactivated.
 */
data class TenantReactivatedEvent(
    val tenantId: UUID,
    val tenantName: String,
    override val occurredAt: Instant
) : DomainEvent {
    override val eventType: String = "TenantReactivated"
}

/**
 * Event published when a tenant's plan is upgraded.
 */
data class TenantPlanUpgradedEvent(
    val tenantId: UUID,
    val tenantName: String,
    val oldPlan: String,
    val newPlan: String,
    override val occurredAt: Instant
) : DomainEvent {
    override val eventType: String = "TenantPlanUpgraded"
}

/**
 * Event published when a tenant's plan is downgraded.
 */
data class TenantPlanDowngradedEvent(
    val tenantId: UUID,
    val tenantName: String,
    val oldPlan: String,
    val newPlan: String,
    override val occurredAt: Instant
) : DomainEvent {
    override val eventType: String = "TenantPlanDowngraded"
}

/**
 * Event published when a tenant's subscription is cancelled.
 */
data class TenantSubscriptionCancelledEvent(
    val tenantId: UUID,
    val tenantName: String,
    val reason: String,
    override val occurredAt: Instant
) : DomainEvent {
    override val eventType: String = "TenantSubscriptionCancelled"
}

/**
 * Event published when a payment is processed for a tenant.
 */
data class TenantPaymentProcessedEvent(
    val tenantId: UUID,
    val invoiceId: UUID,
    val amount: Double,
    val currency: String,
    override val occurredAt: Instant
) : DomainEvent {
    override val eventType: String = "TenantPaymentProcessed"
}
