package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.util.UUID

/**
 * MaintenanceTicket entity for tracking equipment maintenance and repairs.
 * Linked to Equipment entity.
 */
data class MaintenanceTicket(
    val id: UUID,
    val equipmentId: UUID,
    val branchId: UUID,
    val priority: MaintenancePriority,
    val title: String,
    val description: String,
    val status: MaintenanceStatus,
    val reportedBy: UUID,
    val reportedAt: Instant,
    val assignedTo: UUID?,
    val assignedAt: Instant?,
    val scheduledFor: Instant?,
    val startedAt: Instant?,
    val completedAt: Instant?,
    val closedAt: Instant?,
    val resolutionNotes: String?,
    val estimatedCost: String?,
    val actualCost: String?,
    val attachments: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(title.isNotBlank()) { "Ticket title cannot be blank" }
        require(description.isNotBlank()) { "Ticket description cannot be blank" }

        assignedAt?.let {
            require(assignedTo != null) { "Cannot have assignedAt without assignedTo" }
        }

        startedAt?.let {
            require(assignedTo != null) { "Cannot start ticket without assignment" }
            require(!it.isBefore(reportedAt)) { "Start time cannot be before reported time" }
        }

        completedAt?.let {
            require(startedAt != null) { "Cannot complete ticket without starting it" }
            require(!it.isBefore(startedAt)) { "Completion time cannot be before start time" }
        }

        closedAt?.let {
            require(!it.isBefore(reportedAt)) { "Close time cannot be before reported time" }
        }
    }

    fun isOpen(): Boolean = status == MaintenanceStatus.OPEN

    fun isInProgress(): Boolean = status == MaintenanceStatus.IN_PROGRESS

    fun isCompleted(): Boolean = status == MaintenanceStatus.COMPLETED

    fun isClosed(): Boolean = status == MaintenanceStatus.CLOSED

    fun isCancelled(): Boolean = status == MaintenanceStatus.CANCELLED

    fun isHighPriority(): Boolean = priority == MaintenancePriority.HIGH || priority == MaintenancePriority.CRITICAL

    fun assign(assigneeId: UUID): MaintenanceTicket {
        val now = Instant.now()
        return copy(
            assignedTo = assigneeId,
            assignedAt = now,
            status = MaintenanceStatus.ASSIGNED,
            updatedAt = now
        )
    }

    fun start(): MaintenanceTicket {
        require(assignedTo != null) { "Cannot start unassigned ticket" }
        val now = Instant.now()
        return copy(
            status = MaintenanceStatus.IN_PROGRESS,
            startedAt = now,
            updatedAt = now
        )
    }

    fun complete(resolutionNotes: String, actualCost: String? = null): MaintenanceTicket {
        require(status == MaintenanceStatus.IN_PROGRESS) {
            "Only in-progress tickets can be completed"
        }
        val now = Instant.now()
        return copy(
            status = MaintenanceStatus.COMPLETED,
            completedAt = now,
            resolutionNotes = resolutionNotes,
            actualCost = actualCost ?: this.actualCost,
            updatedAt = now
        )
    }

    fun close(): MaintenanceTicket {
        require(status == MaintenanceStatus.COMPLETED) {
            "Only completed tickets can be closed"
        }
        val now = Instant.now()
        return copy(
            status = MaintenanceStatus.CLOSED,
            closedAt = now,
            updatedAt = now
        )
    }

    fun cancel(reason: String): MaintenanceTicket {
        require(!isCompleted() && !isClosed()) {
            "Cannot cancel completed or closed tickets"
        }
        return copy(
            status = MaintenanceStatus.CANCELLED,
            resolutionNotes = reason,
            closedAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    fun updatePriority(newPriority: MaintenancePriority): MaintenanceTicket {
        return copy(priority = newPriority, updatedAt = Instant.now())
    }

    fun schedule(scheduledFor: Instant): MaintenanceTicket {
        require(!scheduledFor.isBefore(Instant.now())) {
            "Cannot schedule for a past time"
        }
        return copy(scheduledFor = scheduledFor, updatedAt = Instant.now())
    }

    fun addAttachment(attachmentUrl: String): MaintenanceTicket {
        return copy(
            attachments = attachments + attachmentUrl,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun create(
            equipmentId: UUID,
            branchId: UUID,
            priority: MaintenancePriority,
            title: String,
            description: String,
            reportedBy: UUID
        ): MaintenanceTicket {
            val now = Instant.now()
            return MaintenanceTicket(
                id = UUID.randomUUID(),
                equipmentId = equipmentId,
                branchId = branchId,
                priority = priority,
                title = title,
                description = description,
                status = MaintenanceStatus.OPEN,
                reportedBy = reportedBy,
                reportedAt = now,
                assignedTo = null,
                assignedAt = null,
                scheduledFor = null,
                startedAt = null,
                completedAt = null,
                closedAt = null,
                resolutionNotes = null,
                estimatedCost = null,
                actualCost = null,
                attachments = emptyList(),
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Maintenance priority enumeration
 */
enum class MaintenancePriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Maintenance ticket status enumeration
 */
enum class MaintenanceStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CLOSED,
    CANCELLED
}
