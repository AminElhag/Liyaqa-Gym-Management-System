package com.liyaqa.gym.presentation.dto.notification

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.time.Instant
import java.util.UUID

// ==================== REQUEST DTOs ====================

/**
 * Request to send notification to members
 */
@Schema(description = "Request to send notification to members")
data class SendNotificationRequest(
    @field:NotBlank(message = "Notification type is required")
    @Schema(description = "Notification type", example = "ANNOUNCEMENT", allowableValues = ["ANNOUNCEMENT", "REMINDER", "ALERT", "PROMOTIONAL", "SYSTEM"])
    val type: String,

    @field:NotBlank(message = "Title is required")
    @field:Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Schema(description = "Notification title", example = "New Class Schedule Available")
    val title: String,

    @field:NotBlank(message = "Message is required")
    @field:Size(min = 10, max = 1000, message = "Message must be between 10 and 1000 characters")
    @Schema(description = "Notification message", example = "Check out our new yoga classes starting next week!")
    val message: String,

    @field:NotNull(message = "Targeting criteria is required")
    @Schema(description = "Targeting criteria for recipients")
    val targeting: NotificationTargeting,

    @field:NotEmpty(message = "At least one channel is required")
    @Schema(description = "Delivery channels", example = "[\"IN_APP\", \"EMAIL\"]")
    val channels: List<String>,

    @Schema(description = "Scheduled send time (null for immediate)", example = "2025-11-20T10:00:00Z")
    val scheduledFor: Instant? = null,

    @Schema(description = "Action button configuration (optional)")
    val action: NotificationAction? = null,

    @Schema(description = "Additional data payload (optional)")
    val data: Map<String, Any>? = null
)

/**
 * Notification targeting criteria
 */
@Schema(description = "Criteria for targeting notification recipients")
data class NotificationTargeting(
    @field:NotBlank(message = "Target type is required")
    @Schema(description = "Target type", example = "SPECIFIC", allowableValues = ["ALL", "SPECIFIC", "BRANCH", "SUBSCRIPTION_TYPE", "ACTIVE_SUBSCRIPTION", "ROLE"])
    val targetType: String,

    @Schema(description = "Specific member IDs (required if targetType is SPECIFIC)")
    val memberIds: List<UUID>? = null,

    @Schema(description = "Branch ID (required if targetType is BRANCH)")
    val branchId: UUID? = null,

    @Schema(description = "Subscription type (required if targetType is SUBSCRIPTION_TYPE)")
    val subscriptionType: String? = null,

    @Schema(description = "Role (required if targetType is ROLE)")
    val role: String? = null
)

/**
 * Notification action configuration
 */
@Schema(description = "Action button for notification")
data class NotificationAction(
    @field:NotBlank(message = "Action label is required")
    @Schema(description = "Button label", example = "View Schedule")
    val label: String,

    @field:NotBlank(message = "Action URL is required")
    @Schema(description = "Action URL or deep link", example = "/classes/schedule")
    val url: String
)

/**
 * Request to update notification preferences
 */
@Schema(description = "Request to update notification preferences")
data class UpdateNotificationPreferencesRequest(
    @Schema(description = "Enable email notifications", example = "true")
    val emailEnabled: Boolean? = null,

    @Schema(description = "Enable SMS notifications", example = "false")
    val smsEnabled: Boolean? = null,

    @Schema(description = "Enable push notifications", example = "true")
    val pushEnabled: Boolean? = null,

    @Schema(description = "Enable in-app notifications", example = "true")
    val inAppEnabled: Boolean? = null,

    @Schema(description = "Notification type preferences")
    val preferences: Map<String, Boolean>? = null
)

// ==================== RESPONSE DTOs ====================

/**
 * Notification response
 */
@Schema(description = "Notification details")
data class NotificationResponse(
    @Schema(description = "Notification ID")
    val id: UUID,

    @Schema(description = "User/Member ID")
    val userId: UUID,

    @Schema(description = "Notification type")
    val type: String,

    @Schema(description = "Notification title")
    val title: String,

    @Schema(description = "Notification message")
    val message: String,

    @Schema(description = "Whether notification has been read")
    val isRead: Boolean,

    @Schema(description = "When notification was read (null if unread)")
    val readAt: Instant?,

    @Schema(description = "Created at timestamp")
    val createdAt: Instant,

    @Schema(description = "Action button (optional)")
    val action: NotificationAction? = null,

    @Schema(description = "Additional data (optional)")
    val data: Map<String, Any>?
)

/**
 * Paginated notifications response
 */
@Schema(description = "Paginated notifications list")
data class PagedNotificationsResponse(
    @Schema(description = "Notification items")
    val content: List<NotificationResponse>,

    @Schema(description = "Current page number")
    val page: Int,

    @Schema(description = "Page size")
    val size: Int,

    @Schema(description = "Total number of elements")
    val totalElements: Long,

    @Schema(description = "Total number of pages")
    val totalPages: Int,

    @Schema(description = "Whether there is a next page")
    val hasNext: Boolean,

    @Schema(description = "Whether there is a previous page")
    val hasPrevious: Boolean,

    @Schema(description = "Unread notifications count", example = "12")
    val unreadCount: Int
)

/**
 * Response after marking all notifications as read
 */
@Schema(description = "Bulk read operation result")
data class BulkReadResponse(
    @Schema(description = "Number of notifications marked as read", example = "15")
    val markedCount: Int,

    @Schema(description = "Response message")
    val message: String
)

/**
 * Response after sending notification
 */
@Schema(description = "Send notification result")
data class SendNotificationResponse(
    @Schema(description = "Notification ID")
    val notificationId: UUID,

    @Schema(description = "Number of recipients", example = "234")
    val recipientCount: Int,

    @Schema(description = "Sent at timestamp")
    val sentAt: Instant,

    @Schema(description = "Sent by user ID")
    val sentBy: UUID,

    @Schema(description = "Delivery channels used")
    val channels: List<String>,

    @Schema(description = "Response message")
    val message: String
)

/**
 * Notification statistics response
 */
@Schema(description = "Notification statistics")
data class NotificationStatsResponse(
    @Schema(description = "Total notifications sent", example = "5678")
    val totalSent: Int,

    @Schema(description = "Total notifications read", example = "4321")
    val totalRead: Int,

    @Schema(description = "Read rate percentage", example = "76.1")
    val readRate: Double,

    @Schema(description = "Average time to read in minutes", example = "45")
    val averageReadTime: Int,

    @Schema(description = "Notifications breakdown by type")
    val notificationsByType: Map<String, Int>,

    @Schema(description = "Notifications breakdown by channel")
    val notificationsByChannel: Map<String, Int>,

    @Schema(description = "Engagement trend over time")
    val engagementTrend: List<EngagementDataPoint>
)

/**
 * Engagement data point
 */
@Schema(description = "Notification engagement at a specific time")
data class EngagementDataPoint(
    @Schema(description = "Date")
    val date: Instant,

    @Schema(description = "Notifications sent")
    val sent: Int,

    @Schema(description = "Notifications read")
    val read: Int,

    @Schema(description = "Read rate percentage")
    val readRate: Double
)

/**
 * Notification preferences response
 */
@Schema(description = "User notification preferences")
data class NotificationPreferencesResponse(
    @Schema(description = "User ID")
    val userId: UUID,

    @Schema(description = "Email notifications enabled")
    val emailEnabled: Boolean,

    @Schema(description = "SMS notifications enabled")
    val smsEnabled: Boolean,

    @Schema(description = "Push notifications enabled")
    val pushEnabled: Boolean,

    @Schema(description = "In-app notifications enabled")
    val inAppEnabled: Boolean,

    @Schema(description = "Notification type preferences")
    val preferences: Map<String, Boolean>
)
