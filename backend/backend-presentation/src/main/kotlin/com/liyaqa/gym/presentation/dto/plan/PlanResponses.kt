package com.liyaqa.gym.presentation.dto.plan

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Response DTO for membership plan details
 */
@Schema(description = "Membership plan details response")
data class PlanResponse(
    @Schema(description = "Plan ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Branch ID", example = "123e4567-e89b-12d3-a456-426614174001")
    val branchId: UUID,

    @Schema(description = "Plan name", example = "Gold Membership")
    val name: String,

    @Schema(description = "Plan description")
    val description: String?,

    @Schema(description = "Plan type", example = "DURATION", allowableValues = ["DURATION", "VISIT_BASED", "TIME_RESTRICTED"])
    val type: String,

    @Schema(description = "Plan price amount", example = "299.99")
    val price: BigDecimal,

    @Schema(description = "Currency code", example = "SAR")
    val currency: String,

    @Schema(description = "Duration in days", example = "30")
    val durationDays: Int?,

    @Schema(description = "Number of visits", example = "10")
    val visitCount: Int?,

    @Schema(description = "Allowed time slots", example = "[\"06:00-12:00\"]")
    val allowedTimeSlots: List<String>?,

    @Schema(description = "Plan features")
    val features: List<String>,

    @Schema(description = "Is plan active", example = "true")
    val isActive: Boolean,

    @Schema(description = "Maximum concurrent active subscriptions")
    val maxActiveSubscriptions: Int?,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant,

    @Schema(description = "Last update timestamp")
    val updatedAt: Instant
)

/**
 * Summary response for plan lists
 */
@Schema(description = "Membership plan summary for list views")
data class PlanSummaryResponse(
    @Schema(description = "Plan ID")
    val id: UUID,

    @Schema(description = "Branch ID")
    val branchId: UUID,

    @Schema(description = "Plan name")
    val name: String,

    @Schema(description = "Plan type")
    val type: String,

    @Schema(description = "Plan price amount")
    val price: BigDecimal,

    @Schema(description = "Currency code")
    val currency: String,

    @Schema(description = "Duration in days")
    val durationDays: Int?,

    @Schema(description = "Number of visits")
    val visitCount: Int?,

    @Schema(description = "Is plan active")
    val isActive: Boolean
)
