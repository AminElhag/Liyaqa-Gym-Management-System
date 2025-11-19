package com.liyaqa.gym.presentation.dto.common

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Standardized API response wrapper
 * All API endpoints return this consistent response format
 */
@Schema(description = "Standard API response wrapper")
data class ApiResponse<T>(
    @Schema(description = "Indicates if the request was successful", example = "true")
    val success: Boolean,

    @Schema(description = "Response data (present when success is true)")
    val data: T? = null,

    @Schema(description = "Error details (present when success is false)")
    val error: ErrorDetails? = null,

    @Schema(description = "Response timestamp", example = "2025-11-19T10:30:00Z")
    val timestamp: Instant = Instant.now()
) {
    companion object {
        /**
         * Create a successful response with data
         */
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                error = null
            )
        }

        /**
         * Create a successful response without data (for operations like delete)
         */
        fun <T> success(): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = null,
                error = null
            )
        }

        /**
         * Create an error response
         */
        fun <T> error(
            message: String,
            code: String,
            details: Map<String, Any>? = null
        ): ApiResponse<T> {
            return ApiResponse(
                success = false,
                data = null,
                error = ErrorDetails(
                    code = code,
                    message = message,
                    details = details
                )
            )
        }
    }
}

/**
 * Error details included in error responses
 */
@Schema(description = "Error details")
data class ErrorDetails(
    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    val code: String,

    @Schema(description = "Error message", example = "Invalid input data")
    val message: String,

    @Schema(description = "Additional error details")
    val details: Map<String, Any>? = null
)
