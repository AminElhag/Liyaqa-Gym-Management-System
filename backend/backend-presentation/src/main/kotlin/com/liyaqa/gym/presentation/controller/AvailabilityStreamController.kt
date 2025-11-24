package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.repositories.ClassRepository
import com.liyaqa.gym.domain.repositories.ClassScheduleRepository
import com.liyaqa.gym.presentation.dto.classmanagement.AvailabilityResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * REST Controller for real-time availability streaming using Server-Sent Events (SSE).
 * Provides real-time updates on class schedule availability.
 */
@RestController
@RequestMapping("/api/v1/availability")
@Tag(name = "Real-time Availability", description = "Real-time availability streaming endpoints")
class AvailabilityStreamController(
    private val scheduleRepository: ClassScheduleRepository,
    private val classRepository: ClassRepository
) {

    private val logger = LoggerFactory.getLogger(AvailabilityStreamController::class.java)

    // Store active SSE connections by schedule ID
    private val emitters = ConcurrentHashMap<UUID, MutableSet<SseEmitter>>()

    // Executor for periodic updates
    private val executor = Executors.newScheduledThreadPool(1)

    init {
        // Schedule periodic availability updates every 5 seconds
        executor.scheduleAtFixedRate({
            broadcastAvailabilityUpdates()
        }, 5, 5, TimeUnit.SECONDS)
    }

    /**
     * Stream real-time availability updates for a schedule using SSE
     */
    @GetMapping("/stream/{scheduleId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @Operation(
        summary = "Stream availability updates",
        description = "Subscribe to real-time availability updates for a class schedule using Server-Sent Events (SSE)"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "SSE stream established",
                content = [Content(schema = Schema(implementation = AvailabilityResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            )
        ]
    )
    fun streamAvailability(
        @Parameter(description = "Schedule ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable scheduleId: UUID
    ): SseEmitter {
        logger.info("New SSE connection for schedule: $scheduleId")

        // Verify schedule exists
        val scheduleOptional = scheduleRepository.findById(scheduleId).getOrThrow()
        if (!scheduleOptional.isPresent) {
            throw ResourceNotFoundException("Schedule not found with ID: $scheduleId")
        }

        // Create SSE emitter with 30-minute timeout
        val emitter = SseEmitter(1800000L) // 30 minutes

        // Add emitter to the set for this schedule
        emitters.computeIfAbsent(scheduleId) { ConcurrentHashMap.newKeySet() }.add(emitter)

        // Remove emitter on completion or timeout
        emitter.onCompletion {
            logger.info("SSE connection completed for schedule: $scheduleId")
            removeEmitter(scheduleId, emitter)
        }

        emitter.onTimeout {
            logger.info("SSE connection timed out for schedule: $scheduleId")
            removeEmitter(scheduleId, emitter)
        }

        emitter.onError { error ->
            logger.error("SSE connection error for schedule: $scheduleId", error)
            removeEmitter(scheduleId, emitter)
        }

        // Send initial availability data
        try {
            val availability = getAvailabilityData(scheduleId)
            emitter.send(
                SseEmitter.event()
                    .name("availability")
                    .data(availability)
            )
        } catch (e: IOException) {
            logger.error("Error sending initial availability data", e)
            removeEmitter(scheduleId, emitter)
        }

        return emitter
    }

    /**
     * Manually trigger availability update broadcast (for testing or immediate updates)
     */
    @PostMapping("/broadcast/{scheduleId}")
    @Operation(
        summary = "Broadcast availability update",
        description = "Manually trigger an availability update broadcast for a schedule"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Broadcast triggered successfully"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            )
        ]
    )
    fun broadcastUpdate(
        @Parameter(description = "Schedule ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable scheduleId: UUID
    ): String {
        logger.info("Manual broadcast triggered for schedule: $scheduleId")

        val scheduleOptional = scheduleRepository.findById(scheduleId).getOrThrow()
        if (!scheduleOptional.isPresent) {
            throw ResourceNotFoundException("Schedule not found with ID: $scheduleId")
        }

        broadcastAvailabilityForSchedule(scheduleId)
        return "Broadcast triggered for schedule: $scheduleId"
    }

    /**
     * Get current active connections count
     */
    @GetMapping("/connections")
    @Operation(
        summary = "Get active connections",
        description = "Get count of active SSE connections per schedule"
    )
    fun getActiveConnections(): Map<String, Int> {
        return emitters.mapValues { it.value.size }
            .mapKeys { it.key.toString() }
    }

    // Helper methods

    private fun removeEmitter(scheduleId: UUID, emitter: SseEmitter) {
        emitters[scheduleId]?.remove(emitter)
        if (emitters[scheduleId]?.isEmpty() == true) {
            emitters.remove(scheduleId)
        }
    }

    private fun broadcastAvailabilityUpdates() {
        emitters.keys.forEach { scheduleId ->
            try {
                broadcastAvailabilityForSchedule(scheduleId)
            } catch (e: Exception) {
                logger.error("Error broadcasting availability for schedule: $scheduleId", e)
            }
        }
    }

    private fun broadcastAvailabilityForSchedule(scheduleId: UUID) {
        val emitterSet = emitters[scheduleId] ?: return
        if (emitterSet.isEmpty()) return

        try {
            val availability = getAvailabilityData(scheduleId)
            val deadEmitters = mutableSetOf<SseEmitter>()

            emitterSet.forEach { emitter ->
                try {
                    emitter.send(
                        SseEmitter.event()
                            .name("availability")
                            .data(availability)
                    )
                } catch (e: IOException) {
                    logger.warn("Failed to send to emitter, marking for removal", e)
                    deadEmitters.add(emitter)
                } catch (e: IllegalStateException) {
                    logger.warn("Emitter in illegal state, marking for removal", e)
                    deadEmitters.add(emitter)
                }
            }

            // Remove dead emitters
            deadEmitters.forEach { removeEmitter(scheduleId, it) }

        } catch (e: Exception) {
            logger.error("Error getting availability data for schedule: $scheduleId", e)
        }
    }

    private fun getAvailabilityData(scheduleId: UUID): AvailabilityResponse {
        val schedule = scheduleRepository.findById(scheduleId).getOrThrow()
            .orElseThrow { ResourceNotFoundException("Schedule not found") }

        val gymClass = classRepository.findById(schedule.classId).getOrThrow()
            .orElseThrow { ResourceNotFoundException("Class not found") }

        return AvailabilityResponse(
            scheduleId = schedule.id,
            capacity = gymClass.capacity,
            currentBookings = schedule.currentBookings,
            availableSpots = schedule.availableSpots(gymClass.capacity),
            waitingList = schedule.waitingList,
            isFull = schedule.isFull(gymClass.capacity),
            hasWaitingList = schedule.waitingList > 0
        )
    }

    /**
     * Cleanup method called on bean destruction
     */
    @PreDestroy
    fun cleanup() {
        logger.info("Shutting down availability stream controller")
        executor.shutdown()
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
        }

        // Close all emitters
        emitters.values.forEach { emitterSet ->
            emitterSet.forEach { emitter ->
                try {
                    emitter.complete()
                } catch (e: Exception) {
                    logger.warn("Error completing emitter", e)
                }
            }
        }
        emitters.clear()
    }
}
