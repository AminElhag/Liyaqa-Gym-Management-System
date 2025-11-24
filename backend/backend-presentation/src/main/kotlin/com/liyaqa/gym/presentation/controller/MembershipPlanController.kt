package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.MembershipPlan
import com.liyaqa.gym.domain.entities.PlanType
import com.liyaqa.gym.domain.repositories.MembershipPlanRepository
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.gym.presentation.dto.common.ApiResponse
import com.liyaqa.gym.presentation.dto.plan.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

/**
 * REST Controller for membership plan management operations.
 * Handles plan creation, updates, retrieval, and archiving.
 */
@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Membership Plans", description = "Membership plan management endpoints")
class MembershipPlanController(
    private val planRepository: MembershipPlanRepository
) {

    private val logger = LoggerFactory.getLogger(MembershipPlanController::class.java)

    /**
     * List all plans for a branch
     */
    @GetMapping
    @Operation(
        summary = "List all membership plans",
        description = "Get all membership plans for a branch (defaults to active plans only)"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Plans retrieved successfully",
                content = [Content(schema = Schema(implementation = PlanSummaryResponse::class))]
            )
        ]
    )
    fun listPlans(
        @Parameter(description = "Branch ID (optional)")
        @RequestParam(required = false) branchId: UUID?,
        @Parameter(description = "Include inactive plans", example = "false")
        @RequestParam(defaultValue = "false") includeInactive: Boolean,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<PlanSummaryResponse>>> {
        logger.info("Listing plans (branchId: $branchId, includeInactive: $includeInactive, page: $page, size: $size)")

        val plans = if (branchId != null) {
            if (includeInactive) {
                planRepository.findByBranch(branchId, page, size).getOrThrow()
            } else {
                planRepository.findActiveByBranch(branchId, page, size).getOrThrow()
            }
        } else {
            // TODO: Implement findAll in repository
            emptyList()
        }

        val response = plans.map { it.toPlanSummaryResponse() }
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get plan details by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get plan details",
        description = "Retrieve detailed information about a specific membership plan"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Plan retrieved successfully",
                content = [Content(schema = Schema(implementation = PlanResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Plan not found"
            )
        ]
    )
    fun getPlanDetails(
        @Parameter(description = "Plan ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<PlanResponse>> {
        logger.info("Fetching plan details for ID: $id")

        val planOptional = planRepository.findById(id)
            .getOrThrow()

        if (!planOptional.isPresent) {
            throw ResourceNotFoundException("Membership plan not found with ID: $id")
        }

        val response = planOptional.get().toPlanResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Create a new plan (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Create new membership plan",
        description = "Create a new membership plan. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Plan created successfully",
                content = [Content(schema = Schema(implementation = PlanResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required"
            )
        ]
    )
    fun createPlan(
        @Valid @RequestBody request: CreatePlanRequest
    ): ResponseEntity<ApiResponse<PlanResponse>> {
        logger.info("Creating new plan: ${request.name} for branch: ${request.branchId}")

        val plan = when (PlanType.valueOf(request.type)) {
            PlanType.DURATION -> MembershipPlan.createDurationBased(
                branchId = request.branchId,
                name = request.name,
                price = Money(request.price, java.util.Currency.getInstance(request.currency)),
                durationDays = request.durationDays
                    ?: throw IllegalArgumentException("Duration is required for DURATION plans"),
                features = request.features
            ).let {
                if (request.description != null) it.copy(description = request.description) else it
            }.let {
                if (request.maxActiveSubscriptions != null) it.copy(maxActiveSubscriptions = request.maxActiveSubscriptions) else it
            }

            PlanType.VISIT_BASED -> MembershipPlan.createVisitBased(
                branchId = request.branchId,
                name = request.name,
                price = Money(request.price, java.util.Currency.getInstance(request.currency)),
                visitCount = request.visitCount
                    ?: throw IllegalArgumentException("Visit count is required for VISIT_BASED plans"),
                features = request.features
            ).let {
                if (request.description != null) it.copy(description = request.description) else it
            }.let {
                if (request.maxActiveSubscriptions != null) it.copy(maxActiveSubscriptions = request.maxActiveSubscriptions) else it
            }

            PlanType.TIME_RESTRICTED -> MembershipPlan.createTimeRestricted(
                branchId = request.branchId,
                name = request.name,
                price = Money(request.price, java.util.Currency.getInstance(request.currency)),
                durationDays = request.durationDays
                    ?: throw IllegalArgumentException("Duration is required for TIME_RESTRICTED plans"),
                allowedTimeSlots = request.allowedTimeSlots
                    ?: throw IllegalArgumentException("Allowed time slots are required for TIME_RESTRICTED plans"),
                features = request.features
            ).let {
                if (request.description != null) it.copy(description = request.description) else it
            }.let {
                if (request.maxActiveSubscriptions != null) it.copy(maxActiveSubscriptions = request.maxActiveSubscriptions) else it
            }
        }

        val savedPlan = planRepository.save(plan)
            .getOrThrow()

        val response = savedPlan.toPlanResponse()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Update an existing plan (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update membership plan",
        description = "Update an existing membership plan. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Plan updated successfully",
                content = [Content(schema = Schema(implementation = PlanResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Plan not found"
            )
        ]
    )
    fun updatePlan(
        @Parameter(description = "Plan ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdatePlanRequest
    ): ResponseEntity<ApiResponse<PlanResponse>> {
        logger.info("Updating plan ID: $id")

        val planOptional = planRepository.findById(id)
            .getOrThrow()

        if (!planOptional.isPresent) {
            throw ResourceNotFoundException("Membership plan not found with ID: $id")
        }

        var plan = planOptional.get()

        // Apply updates
        request.name?.let { plan = plan.copy(name = it) }
        request.description?.let { plan = plan.copy(description = it) }
        request.durationDays?.let { plan = plan.copy(durationDays = it) }
        request.visitCount?.let { plan = plan.copy(visitCount = it) }
        request.allowedTimeSlots?.let { plan = plan.copy(allowedTimeSlots = it) }
        request.features?.let { plan = plan.copy(features = it) }
        request.maxActiveSubscriptions?.let { plan = plan.copy(maxActiveSubscriptions = it) }

        if (request.price != null && request.currency != null) {
            plan = plan.updatePrice(Money(request.price, java.util.Currency.getInstance(request.currency)))
        }

        request.isActive?.let {
            plan = if (it) plan.activate() else plan.deactivate()
        }

        plan = plan.copy(updatedAt = Instant.now())

        val savedPlan = planRepository.save(plan)
            .getOrThrow()

        val response = savedPlan.toPlanResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Archive (deactivate) a plan (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Archive membership plan",
        description = "Archive (deactivate) a membership plan. Admin only. This does not delete the plan."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Plan archived successfully"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Plan not found"
            )
        ]
    )
    fun archivePlan(
        @Parameter(description = "Plan ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<PlanResponse>> {
        logger.info("Archiving plan ID: $id")

        val planOptional = planRepository.findById(id)
            .getOrThrow()

        if (!planOptional.isPresent) {
            throw ResourceNotFoundException("Membership plan not found with ID: $id")
        }

        val plan = planOptional.get().deactivate()
        val savedPlan = planRepository.save(plan)
            .getOrThrow()

        val response = savedPlan.toPlanResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    // Extension functions for DTO conversion

    private fun MembershipPlan.toPlanResponse(): PlanResponse {
        return PlanResponse(
            id = this.id,
            branchId = this.branchId,
            name = this.name,
            description = this.description,
            type = this.type.name,
            price = this.price.amount,
            currency = this.price.currency.currencyCode,
            durationDays = this.durationDays,
            visitCount = this.visitCount,
            allowedTimeSlots = this.allowedTimeSlots,
            features = this.features,
            isActive = this.isActive,
            maxActiveSubscriptions = this.maxActiveSubscriptions,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun MembershipPlan.toPlanSummaryResponse(): PlanSummaryResponse {
        return PlanSummaryResponse(
            id = this.id,
            branchId = this.branchId,
            name = this.name,
            type = this.type.name,
            price = this.price.amount,
            currency = this.price.currency.currencyCode,
            durationDays = this.durationDays,
            visitCount = this.visitCount,
            isActive = this.isActive
        )
    }
}
