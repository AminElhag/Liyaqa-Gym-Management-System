package com.liyaqa.gym.presentation.dto.plan

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.UUID

/**
 * Request DTO for creating a new membership plan
 */
@Schema(description = "Request to create a new membership plan")
data class CreatePlanRequest(
    @field:NotNull(message = "Branch ID is required")
    @Schema(description = "Branch ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val branchId: UUID,

    @field:NotBlank(message = "Plan name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Plan name", example = "Gold Membership")
    val name: String,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Plan description", example = "Full access to all gym facilities")
    val description: String? = null,

    @field:NotBlank(message = "Plan type is required")
    @Schema(
        description = "Plan type",
        example = "DURATION",
        allowableValues = ["DURATION", "VISIT_BASED", "TIME_RESTRICTED"]
    )
    val type: String,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Schema(description = "Plan price", example = "299.99")
    val price: BigDecimal,

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Schema(description = "Currency code (ISO 4217)", example = "SAR")
    val currency: String,

    @field:Min(value = 1, message = "Duration must be at least 1 day")
    @Schema(description = "Duration in days (required for DURATION and TIME_RESTRICTED)", example = "30")
    val durationDays: Int? = null,

    @field:Min(value = 1, message = "Visit count must be at least 1")
    @Schema(description = "Number of visits (required for VISIT_BASED)", example = "10")
    val visitCount: Int? = null,

    @Schema(description = "Allowed time slots (required for TIME_RESTRICTED)", example = "[\"06:00-12:00\", \"18:00-22:00\"]")
    val allowedTimeSlots: List<String>? = null,

    @Schema(description = "Plan features list", example = "[\"Access to all equipment\", \"Free group classes\", \"Personal locker\"]")
    val features: List<String> = emptyList(),

    @field:Min(value = 1, message = "Max active subscriptions must be at least 1")
    @Schema(description = "Maximum concurrent active subscriptions", example = "100")
    val maxActiveSubscriptions: Int? = null
)

/**
 * Request DTO for updating a membership plan
 */
@Schema(description = "Request to update a membership plan")
data class UpdatePlanRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Plan name", example = "Platinum Membership")
    val name: String? = null,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Plan description")
    val description: String? = null,

    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Schema(description = "Plan price", example = "349.99")
    val price: BigDecimal? = null,

    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Schema(description = "Currency code (ISO 4217)", example = "SAR")
    val currency: String? = null,

    @field:Min(value = 1, message = "Duration must be at least 1 day")
    @Schema(description = "Duration in days", example = "60")
    val durationDays: Int? = null,

    @field:Min(value = 1, message = "Visit count must be at least 1")
    @Schema(description = "Number of visits", example = "20")
    val visitCount: Int? = null,

    @Schema(description = "Allowed time slots", example = "[\"00:00-24:00\"]")
    val allowedTimeSlots: List<String>? = null,

    @Schema(description = "Plan features list")
    val features: List<String>? = null,

    @Schema(description = "Whether plan is active", example = "true")
    val isActive: Boolean? = null,

    @field:Min(value = 1, message = "Max active subscriptions must be at least 1")
    @Schema(description = "Maximum concurrent active subscriptions")
    val maxActiveSubscriptions: Int? = null
)
