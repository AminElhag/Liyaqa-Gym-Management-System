package com.liyaqa.gym.domain.events

import com.liyaqa.gym.domain.entities.MaintenancePriority
import java.time.Instant
import java.util.UUID

/**
 * Event emitted when equipment requires maintenance.
 *
 * @property equipmentId The unique identifier of the equipment requiring maintenance
 * @property priority The priority level of the maintenance
 * @property description Description of the maintenance requirement
 */
data class MaintenanceRequiredEvent(
    val equipmentId: UUID,
    val priority: MaintenancePriority,
    val description: String,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent(eventId, occurredAt) {

    override fun toString(): String {
        return "MaintenanceRequiredEvent(eventId=$eventId, equipmentId=$equipmentId, priority=$priority, description='$description', occurredAt=$occurredAt)"
    }
}
