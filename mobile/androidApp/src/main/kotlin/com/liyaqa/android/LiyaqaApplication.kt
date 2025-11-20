package com.liyaqa.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.liyaqa.android.di.appModule
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

@HiltAndroidApp
class LiyaqaApplication : Application() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin for dependency injection (for shared module)
        initializeKoin()

        // Initialize Firebase
        initializeFirebase()

        // Setup notification channels
        createNotificationChannels()

        // Setup crash reporting
        setupCrashReporting()
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@LiyaqaApplication)
            modules(appModule)
        }
    }

    private fun initializeFirebase() {
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Log app open event
        firebaseAnalytics.logEvent("app_open", null)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Default notification channel
            val defaultChannel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications from Liyaqa"
                enableLights(true)
                enableVibration(true)
            }

            // Booking notifications channel
            val bookingChannel = NotificationChannel(
                CHANNEL_ID_BOOKINGS,
                "Class Bookings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about your class bookings"
                enableLights(true)
                enableVibration(true)
            }

            // Reminders channel
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders about upcoming classes"
                enableLights(true)
                enableVibration(true)
            }

            // Promotions channel
            val promotionsChannel = NotificationChannel(
                CHANNEL_ID_PROMOTIONS,
                "Promotions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Special offers and promotions"
                enableLights(false)
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(
                listOf(defaultChannel, bookingChannel, reminderChannel, promotionsChannel)
            )
        }
    }

    private fun setupCrashReporting() {
        // Enable Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

    companion object {
        const val CHANNEL_ID_DEFAULT = "default_channel"
        const val CHANNEL_ID_BOOKINGS = "bookings_channel"
        const val CHANNEL_ID_REMINDERS = "reminders_channel"
        const val CHANNEL_ID_PROMOTIONS = "promotions_channel"
    }
}
