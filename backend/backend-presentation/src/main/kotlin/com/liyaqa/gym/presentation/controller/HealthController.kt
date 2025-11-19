package com.liyaqa.gym.presentation.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * Health check controller for monitoring application status.
 */
@RestController
@RequestMapping("/api/v1/health")
class HealthController {

    @GetMapping
    fun health(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(
            HealthResponse(
                status = "UP",
                timestamp = LocalDateTime.now(),
                message = "Liyaqa Gym Management System is running"
            )
        )
    }
}

data class HealthResponse(
    val status: String,
    val timestamp: LocalDateTime,
    val message: String
)
