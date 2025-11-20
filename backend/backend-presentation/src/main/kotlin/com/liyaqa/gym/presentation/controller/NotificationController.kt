package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.presentation.dto.notification.*
import com.liyaqa.gym.presentation.dto.common.ApiResponse
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
 * REST Controller for notification management.
 * Handles user notifications, read status, and bulk notification sending for admins.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
class NotificationController {

    private val logger = LoggerFactory.getLogger(NotificationController::class.java)

    /**
     * Get user's notifications with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get user notifications",
        description = "Retrieve paginated list of notifications for the authenticated user."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Notifications retrieved successfully",
                content = [Content(schema = Schema(implementation = PagedNotificationsResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            )
        ]
    )
    fun getUserNotifications(
        @Parameter(description = "Filter by read status (true/false)")
        @RequestParam(required = false) isRead: Boolean?,
        @Parameter(description = "Filter by notification type")
        @RequestParam(required = false) type: String?,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<PagedNotificationsResponse>> {
        logger.info("Fetching notifications - isRead: $isRead, type: $type, page: $page")

        // TODO: Implement via use case
        // - Get current user ID from security context
        // - Query notifications for user
        // - Apply filters (read status, type)
        // - Paginate results
        // - Include unread count

        val response = PagedNotificationsResponse(
            content = emptyList(), // TODO: Get actual notifications
            page = page,
            size = size,
            totalElements = 0,
            totalPages = 0,
            hasNext = false,
            hasPrevious = false,
            unreadCount = 0
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Mark notification as read",
        description = "Mark a specific notification as read."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Notification marked as read successfully",
                content = [Content(schema = Schema(implementation = NotificationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - notification belongs to another user"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Notification not found"
            )
        ]
    )
    fun markAsRead(
        @Parameter(description = "Notification ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<NotificationResponse>> {
        logger.info("Marking notification as read: $id")

        // TODO: Implement via use case
        // - Verify notification belongs to current user
        // - Update read status
        // - Update read timestamp

        val response = NotificationResponse(
            id = id,
            userId = UUID.randomUUID(), // TODO: Get from security context
            type = "INFO",
            title = "Notification Title",
            message = "Notification message",
            isRead = true,
            readAt = Instant.now(),
            createdAt = Instant.now(),
            data = null
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Mark all notifications as read",
        description = "Mark all unread notifications as read for the authenticated user."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "All notifications marked as read successfully",
                content = [Content(schema = Schema(implementation = BulkReadResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            )
        ]
    )
    fun markAllAsRead(): ResponseEntity<ApiResponse<BulkReadResponse>> {
        logger.info("Marking all notifications as read")

        // TODO: Implement via use case
        // - Get current user ID from security context
        // - Find all unread notifications for user
        // - Mark all as read
        // - Return count of marked notifications

        val response = BulkReadResponse(
            markedCount = 0, // TODO: Get actual count
            message = "All notifications marked as read"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Send notification to members (admin only)
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Send notification to members",
        description = "Send a notification to selected members or all members based on targeting criteria. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Notification sent successfully",
                content = [Content(schema = Schema(implementation = SendNotificationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request or targeting criteria"
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
    fun sendNotification(
        @Valid @RequestBody request: SendNotificationRequest
    ): ResponseEntity<ApiResponse<SendNotificationResponse>> {
        logger.info("Sending notification to members - type: ${request.type}, targeting: ${request.targeting}")

        // TODO: Implement via use case
        // - Validate targeting criteria
        // - Resolve member list based on targeting
        // - Create notifications for each member
        // - Send via configured channels (in-app, email, SMS)
        // - Track delivery status

        val recipientCount = when (request.targeting.targetType) {
            "ALL" -> 0 // TODO: Count all active members
            "SPECIFIC" -> request.targeting.memberIds?.size ?: 0
            "BRANCH" -> 0 // TODO: Count members in branch
            "SUBSCRIPTION_TYPE" -> 0 // TODO: Count members with subscription type
            "ACTIVE_SUBSCRIPTION" -> 0 // TODO: Count members with active subscription
            else -> 0
        }

        val response = SendNotificationResponse(
            notificationId = UUID.randomUUID(),
            recipientCount = recipientCount,
            sentAt = Instant.now(),
            sentBy = UUID.randomUUID(), // TODO: Get from security context
            channels = request.channels,
            message = "Notification sent to $recipientCount recipients"
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Get notification statistics (admin only)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get notification statistics",
        description = "Get statistics about notifications including delivery rates and engagement. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Notification statistics retrieved successfully",
                content = [Content(schema = Schema(implementation = NotificationStatsResponse::class))]
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
    fun getNotificationStats(
        @Parameter(description = "Filter by date (from)")
        @RequestParam(required = false) fromDate: Instant?,
        @Parameter(description = "Filter by date (to)")
        @RequestParam(required = false) toDate: Instant?
    ): ResponseEntity<ApiResponse<NotificationStatsResponse>> {
        logger.info("Fetching notification statistics - fromDate: $fromDate, toDate: $toDate")

        // TODO: Implement via use case
        // - Calculate total sent
        // - Calculate read rate
        // - Calculate delivery rate
        // - Break down by type and channel

        val response = NotificationStatsResponse(
            totalSent = 0,
            totalRead = 0,
            readRate = 0.0,
            averageReadTime = 0,
            notificationsByType = emptyMap(),
            notificationsByChannel = emptyMap(),
            engagementTrend = emptyList()
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Delete notification",
        description = "Delete a notification for the authenticated user."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Notification deleted successfully"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - notification belongs to another user"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Notification not found"
            )
        ]
    )
    fun deleteNotification(
        @Parameter(description = "Notification ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Deleting notification: $id")

        // TODO: Implement via use case
        // - Verify notification belongs to current user
        // - Soft delete or hard delete based on policy
        // - Update user's notification count

        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"))
    }

    /**
     * Get notification preferences (future feature)
     */
    @GetMapping("/preferences")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get notification preferences",
        description = "Get notification preferences for the authenticated user."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Notification preferences retrieved successfully",
                content = [Content(schema = Schema(implementation = NotificationPreferencesResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            )
        ]
    )
    fun getNotificationPreferences(): ResponseEntity<ApiResponse<NotificationPreferencesResponse>> {
        logger.info("Fetching notification preferences")

        // TODO: Implement notification preferences feature
        // - Get user preferences
        // - Include channels (email, SMS, push, in-app)
        // - Include notification types preferences

        val response = NotificationPreferencesResponse(
            userId = UUID.randomUUID(), // TODO: Get from security context
            emailEnabled = true,
            smsEnabled = false,
            pushEnabled = true,
            inAppEnabled = true,
            preferences = mapOf(
                "BOOKING_CONFIRMATION" to true,
                "PAYMENT_REMINDER" to true,
                "CLASS_REMINDER" to true,
                "SUBSCRIPTION_EXPIRY" to true,
                "PROMOTIONAL" to false
            )
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Update notification preferences (future feature)
     */
    @PutMapping("/preferences")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update notification preferences",
        description = "Update notification preferences for the authenticated user."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Notification preferences updated successfully",
                content = [Content(schema = Schema(implementation = NotificationPreferencesResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid preferences"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            )
        ]
    )
    fun updateNotificationPreferences(
        @Valid @RequestBody request: UpdateNotificationPreferencesRequest
    ): ResponseEntity<ApiResponse<NotificationPreferencesResponse>> {
        logger.info("Updating notification preferences")

        // TODO: Implement notification preferences update
        // - Validate preferences
        // - Update user preferences
        // - Return updated preferences

        val response = NotificationPreferencesResponse(
            userId = UUID.randomUUID(), // TODO: Get from security context
            emailEnabled = request.emailEnabled ?: true,
            smsEnabled = request.smsEnabled ?: false,
            pushEnabled = request.pushEnabled ?: true,
            inAppEnabled = request.inAppEnabled ?: true,
            preferences = request.preferences ?: emptyMap()
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
