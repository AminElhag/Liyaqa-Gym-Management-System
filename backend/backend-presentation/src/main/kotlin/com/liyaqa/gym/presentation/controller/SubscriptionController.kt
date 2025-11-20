package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.application.subscription.*
import com.liyaqa.gym.application.subscription.commands.*
import com.liyaqa.gym.application.subscription.dto.FreezeConfirmationDTO
import com.liyaqa.gym.application.subscription.dto.SubscriptionDTO
import com.liyaqa.gym.application.subscription.dto.SubscriptionMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.PaymentMethod
import com.liyaqa.gym.domain.entities.SubscriptionStatus
import com.liyaqa.gym.domain.repositories.SubscriptionRepository
import com.liyaqa.gym.presentation.dto.common.ApiResponse
import com.liyaqa.gym.presentation.dto.subscription.*
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
import java.time.LocalDate
import java.util.UUID

/**
 * REST Controller for subscription management operations.
 * Handles subscription creation, renewal, freezing, cancellation, and upgrades.
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Subscription management endpoints")
class SubscriptionController(
    private val createSubscriptionUseCase: CreateSubscriptionUseCase,
    private val renewSubscriptionUseCase: RenewSubscriptionUseCase,
    private val freezeSubscriptionUseCase: FreezeSubscriptionUseCase,
    private val cancelSubscriptionUseCase: CancelSubscriptionUseCase,
    private val upgradeSubscriptionUseCase: UpgradeSubscriptionUseCase,
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionMapper: SubscriptionMapper
) {

    private val logger = LoggerFactory.getLogger(SubscriptionController::class.java)

    /**
     * Create a new subscription
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Create new subscription",
        description = "Create a new subscription for a member with a membership plan. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Subscription created successfully",
                content = [Content(schema = Schema(implementation = SubscriptionResponse::class))]
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
                description = "Forbidden - staff or admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Member or plan not found"
            )
        ]
    )
    fun createSubscription(
        @Valid @RequestBody request: CreateSubscriptionRequest
    ): ResponseEntity<ApiResponse<SubscriptionResponse>> {
        logger.info("Creating subscription for member: ${request.memberId} with plan: ${request.planId}")

        val command = CreateSubscriptionCommand(
            memberId = request.memberId,
            planId = request.planId,
            startDate = request.startDate ?: LocalDate.now(),
            autoRenew = request.autoRenew,
            paymentMethod = PaymentMethod.valueOf(request.paymentMethod),
            paymentMetadata = request.paymentMetadata
        )

        val result = createSubscriptionUseCase.execute(command)
            .getOrThrow()

        val response = result.toSubscriptionResponse()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Get subscription details by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get subscription details",
        description = "Retrieve detailed information about a specific subscription"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Subscription retrieved successfully",
                content = [Content(schema = Schema(implementation = SubscriptionResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Subscription not found"
            )
        ]
    )
    fun getSubscriptionDetails(
        @Parameter(description = "Subscription ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<SubscriptionResponse>> {
        logger.info("Fetching subscription details for ID: $id")

        val subscriptionOptional = subscriptionRepository.findById(id)
            .getOrThrow()

        if (!subscriptionOptional.isPresent) {
            throw ResourceNotFoundException("Subscription not found with ID: $id")
        }

        val subscription = subscriptionMapper.toDTO(subscriptionOptional.get())
        val response = subscription.toSubscriptionResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Renew a subscription
     */
    @PutMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Renew subscription",
        description = "Renew an existing subscription for another period"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Subscription renewed successfully",
                content = [Content(schema = Schema(implementation = RenewalConfirmationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request or subscription cannot be renewed"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Subscription not found"
            )
        ]
    )
    fun renewSubscription(
        @Parameter(description = "Subscription ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: RenewSubscriptionRequest
    ): ResponseEntity<ApiResponse<RenewalConfirmationResponse>> {
        logger.info("Renewing subscription ID: $id")

        val command = RenewSubscriptionCommand(
            subscriptionId = id,
            paymentMethod = PaymentMethod.valueOf(request.paymentMethod),
            paymentMetadata = request.paymentMetadata
        )

        val result = renewSubscriptionUseCase.execute(command)
            .getOrThrow()

        val response = RenewalConfirmationResponse(
            subscriptionId = result.id,
            newEndDate = result.endDate!!,
            paymentId = UUID.randomUUID(), // TODO: Get actual payment ID from result
            message = "Subscription renewed successfully"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Freeze a subscription
     */
    @PutMapping("/{id}/freeze")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Freeze subscription",
        description = "Temporarily freeze/pause a subscription for a specified duration"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Subscription frozen successfully",
                content = [Content(schema = Schema(implementation = FreezeConfirmationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request or subscription cannot be frozen"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Subscription not found"
            )
        ]
    )
    fun freezeSubscription(
        @Parameter(description = "Subscription ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: FreezeSubscriptionRequest
    ): ResponseEntity<ApiResponse<FreezeConfirmationResponse>> {
        logger.info("Freezing subscription ID: $id for ${request.duration} days")

        val freezeUntil = LocalDate.now().plusDays(request.duration.toLong())

        val command = FreezeSubscriptionCommand(
            subscriptionId = id,
            freezeUntil = freezeUntil,
            reason = request.reason
        )

        val result = freezeSubscriptionUseCase.execute(command)
            .getOrThrow()

        val response = result.toFreezeConfirmationResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Cancel a subscription
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Cancel subscription",
        description = "Cancel a subscription either immediately or at the end of the current period"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Subscription cancelled successfully",
                content = [Content(schema = Schema(implementation = CancellationConfirmationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request or subscription cannot be cancelled"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Subscription not found"
            )
        ]
    )
    fun cancelSubscription(
        @Parameter(description = "Subscription ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: CancelSubscriptionRequest
    ): ResponseEntity<ApiResponse<CancellationConfirmationResponse>> {
        logger.info("Cancelling subscription ID: $id (immediate: ${request.immediate})")

        val command = CancelSubscriptionCommand(
            subscriptionId = id,
            reason = request.reason,
            immediate = request.immediate
        )

        val result = cancelSubscriptionUseCase.execute(command)
            .getOrThrow()

        val response = CancellationConfirmationResponse(
            subscriptionId = result.id,
            cancelledAt = result.cancelledAt!!,
            immediate = request.immediate,
            endDate = result.endDate,
            message = if (request.immediate) "Subscription cancelled immediately" else "Subscription will end at ${result.endDate}"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Upgrade subscription to a different plan
     */
    @PutMapping("/{id}/upgrade")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Upgrade subscription",
        description = "Upgrade subscription to a different membership plan"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Subscription upgraded successfully",
                content = [Content(schema = Schema(implementation = UpgradeConfirmationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request or subscription cannot be upgraded"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Subscription or new plan not found"
            )
        ]
    )
    fun upgradeSubscription(
        @Parameter(description = "Subscription ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpgradeSubscriptionRequest
    ): ResponseEntity<ApiResponse<UpgradeConfirmationResponse>> {
        logger.info("Upgrading subscription ID: $id to plan: ${request.planId}")

        val command = UpgradeSubscriptionCommand(
            subscriptionId = id,
            newPlanId = request.planId,
            paymentMethod = PaymentMethod.valueOf(request.paymentMethod),
            paymentMetadata = request.paymentMetadata
        )

        val result = upgradeSubscriptionUseCase.execute(command)
            .getOrThrow()

        val response = UpgradeConfirmationResponse(
            subscriptionId = result.id,
            newPlanId = result.planId,
            paymentId = null, // TODO: Get actual payment ID from result
            newEndDate = result.endDate,
            message = "Subscription upgraded successfully"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get expiring subscriptions (within 7 days)
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get expiring subscriptions",
        description = "Get subscriptions expiring within the next 7 days. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Expiring subscriptions retrieved successfully",
                content = [Content(schema = Schema(implementation = PagedSubscriptionResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - staff or admin role required"
            )
        ]
    )
    fun getExpiringSubscriptions(
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<SubscriptionSummaryResponse>>> {
        logger.info("Fetching expiring subscriptions (page: $page, size: $size)")

        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(7)

        val subscriptions = subscriptionRepository.findExpiringBetween(startDate, endDate, page, size)
            .getOrThrow()

        val response = subscriptions.map { subscription ->
            subscriptionMapper.toDTO(subscription).toSubscriptionSummaryResponse()
        }

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * List all subscriptions with filtering and pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "List all subscriptions",
        description = "Get all subscriptions with pagination and filtering by status. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Subscriptions retrieved successfully",
                content = [Content(schema = Schema(implementation = PagedSubscriptionResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - staff or admin role required"
            )
        ]
    )
    fun listSubscriptions(
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) status: String?,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<SubscriptionSummaryResponse>>> {
        logger.info("Listing subscriptions (status: $status, page: $page, size: $size)")

        val subscriptions = if (status != null) {
            subscriptionRepository.findByStatus(SubscriptionStatus.valueOf(status), page, size)
                .getOrThrow()
        } else {
            // TODO: Implement findAll in repository
            emptyList()
        }

        val response = subscriptions.map { subscription ->
            subscriptionMapper.toDTO(subscription).toSubscriptionSummaryResponse()
        }

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    // Extension functions for DTO conversion

    private fun SubscriptionDTO.toSubscriptionResponse(): SubscriptionResponse {
        return SubscriptionResponse(
            id = this.id,
            memberId = this.memberId,
            planId = this.planId,
            startDate = this.startDate,
            endDate = this.endDate,
            status = this.status.name,
            autoRenew = this.autoRenew,
            remainingVisits = this.remainingVisits,
            pausedAt = this.pausedAt,
            pausedUntil = this.pausedUntil,
            cancelledAt = this.cancelledAt,
            cancellationReason = this.cancellationReason,
            isActive = this.isActive,
            isExpired = this.isExpired,
            canUse = this.canUse,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun SubscriptionDTO.toSubscriptionSummaryResponse(): SubscriptionSummaryResponse {
        return SubscriptionSummaryResponse(
            id = this.id,
            memberId = this.memberId,
            planId = this.planId,
            startDate = this.startDate,
            endDate = this.endDate,
            status = this.status.name,
            autoRenew = this.autoRenew,
            canUse = this.canUse
        )
    }

    private fun FreezeConfirmationDTO.toFreezeConfirmationResponse(): FreezeConfirmationResponse {
        return FreezeConfirmationResponse(
            subscriptionId = this.subscriptionId,
            frozenUntil = this.frozenUntil,
            newEndDate = this.newEndDate,
            freezeDays = this.freezeDays
        )
    }
}
