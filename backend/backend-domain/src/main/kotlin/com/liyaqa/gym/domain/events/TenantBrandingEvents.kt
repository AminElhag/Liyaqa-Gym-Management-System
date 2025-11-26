package com.liyaqa.gym.domain.events

import java.time.Instant
import java.util.UUID

/**
 * Event published when tenant branding is updated.
 */
data class TenantBrandingUpdatedEvent(
    val tenantId: UUID,
    override val occurredAt: Instant
) : DomainEvent
