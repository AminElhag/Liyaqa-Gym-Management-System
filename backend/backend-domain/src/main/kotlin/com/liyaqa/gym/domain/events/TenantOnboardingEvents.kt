package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event published when tenant onboarding is completed
 */
data class TenantOnboardingCompletedEvent(
    val tenantId: UUID,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent

/**
 * Event published when a tenant is activated after completing onboarding
 */
data class TenantActivatedEvent(
    val tenantId: UUID,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent

/**
 * Event published when an onboarding step is completed
 */
data class OnboardingStepCompletedEvent(
    val tenantId: UUID,
    val step: String,
    val data: Map<String, Any>?,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent
