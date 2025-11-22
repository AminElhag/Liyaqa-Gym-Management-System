package com.liyaqa.android.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.liyaqa.android.LiyaqaApplication
import com.liyaqa.android.MainActivity
import kotlin.random.Random

/**
 * Firebase Cloud Messaging service for handling push notifications
 */
class LiyaqaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Send token to backend server
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Handle data payload
        message.data.isNotEmpty().let {
            handleDataPayload(message.data)
        }

        // Handle notification payload
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "Liyaqa",
                body = notification.body ?: "",
                data = message.data
            )
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"] ?: return

        when (type) {
            "booking_confirmation" -> {
                // Handle booking confirmation
                val bookingId = data["booking_id"]
                val classId = data["class_id"]
                showNotification(
                    title = "Booking Confirmed",
                    body = "Your class booking has been confirmed!",
                    data = data,
                    channelId = LiyaqaApplication.CHANNEL_ID_BOOKINGS
                )
            }
            "booking_reminder" -> {
                // Handle booking reminder
                val classTime = data["class_time"]
                showNotification(
                    title = "Class Reminder",
                    body = "Your class starts in 1 hour!",
                    data = data,
                    channelId = LiyaqaApplication.CHANNEL_ID_REMINDERS
                )
            }
            "booking_cancelled" -> {
                // Handle booking cancellation
                showNotification(
                    title = "Booking Cancelled",
                    body = data["message"] ?: "Your booking has been cancelled.",
                    data = data,
                    channelId = LiyaqaApplication.CHANNEL_ID_BOOKINGS
                )
            }
            "promotion" -> {
                // Handle promotion
                showNotification(
                    title = data["title"] ?: "Special Offer",
                    body = data["message"] ?: "",
                    data = data,
                    channelId = LiyaqaApplication.CHANNEL_ID_PROMOTIONS
                )
            }
            else -> {
                // Handle unknown notification type
                showNotification(
                    title = data["title"] ?: "Liyaqa",
                    body = data["message"] ?: "",
                    data = data
                )
            }
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        channelId: String = LiyaqaApplication.CHANNEL_ID_DEFAULT
    ) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create intent for deep link
        val intent = createDeepLinkIntent(data)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            Random.nextInt(),
            intent,
            pendingIntentFlags
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using Android system icon as placeholder
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }

    private fun createDeepLinkIntent(data: Map<String, String>): Intent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Add deep link data
        when (data["type"]) {
            "booking_confirmation", "booking_reminder", "booking_cancelled" -> {
                data["booking_id"]?.let { bookingId ->
                    intent.putExtra("deep_link", "liyaqa://booking/$bookingId")
                }
            }
            "promotion" -> {
                data["promotion_id"]?.let { promotionId ->
                    intent.putExtra("deep_link", "liyaqa://promotion/$promotionId")
                }
            }
        }

        return intent
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Send the FCM token to your backend server
        // This should be implemented when the API client is set up
        android.util.Log.d("FCM", "New token: $token")
    }
}
