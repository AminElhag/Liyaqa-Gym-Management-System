package com.liyaqa.android.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.liyaqa.android.ui.theme.LiyaqaBrand

/**
 * Notification Settings Screen
 * Allows users to configure notification preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val notificationSettingsState by viewModel.notificationSettingsState.collectAsState()

    var classReminders by remember { mutableStateOf(true) }
    var paymentReminders by remember { mutableStateOf(true) }
    var promotionalNotifications by remember { mutableStateOf(false) }
    var bookingConfirmations by remember { mutableStateOf(true) }
    var pushEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(true) }
    var smsEnabled by remember { mutableStateOf(false) }

    // Load preferences when screen loads
    LaunchedEffect(Unit) {
        viewModel.loadNotificationPreferences()
    }

    // Initialize toggles with loaded preferences
    LaunchedEffect(notificationSettingsState.preferences) {
        val prefs = notificationSettingsState.preferences
        classReminders = prefs.classReminders
        paymentReminders = prefs.paymentReminders
        promotionalNotifications = prefs.promotionalNotifications
        bookingConfirmations = prefs.bookingConfirmations
        pushEnabled = prefs.pushEnabled
        emailEnabled = prefs.emailEnabled
        smsEnabled = prefs.smsEnabled
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Notification Types Section
            SectionHeader(title = "Notification Types")

            Spacer(modifier = Modifier.height(8.dp))

            NotificationToggleItem(
                title = "Class Reminders",
                description = "Get reminders before your scheduled classes",
                checked = classReminders,
                onCheckedChange = { classReminders = it }
            )

            NotificationToggleItem(
                title = "Payment Reminders",
                description = "Get notified about upcoming payments",
                checked = paymentReminders,
                onCheckedChange = { paymentReminders = it }
            )

            NotificationToggleItem(
                title = "Promotional Notifications",
                description = "Receive updates about offers and promotions",
                checked = promotionalNotifications,
                onCheckedChange = { promotionalNotifications = it }
            )

            NotificationToggleItem(
                title = "Booking Confirmations",
                description = "Get confirmation when you book a class",
                checked = bookingConfirmations,
                onCheckedChange = { bookingConfirmations = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Channel Preferences Section
            SectionHeader(title = "Channel Preferences")

            Spacer(modifier = Modifier.height(8.dp))

            NotificationToggleItem(
                title = "Push Notifications",
                description = "Receive notifications on your device",
                checked = pushEnabled,
                onCheckedChange = { pushEnabled = it }
            )

            NotificationToggleItem(
                title = "Email Notifications",
                description = "Receive notifications via email",
                checked = emailEnabled,
                onCheckedChange = { emailEnabled = it }
            )

            NotificationToggleItem(
                title = "SMS Notifications",
                description = "Receive notifications via text message",
                checked = smsEnabled,
                onCheckedChange = { smsEnabled = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message
            if (notificationSettingsState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = notificationSettingsState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Success Message
            if (notificationSettingsState.successMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LiyaqaBrand.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = notificationSettingsState.successMessage!!,
                        color = LiyaqaBrand,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    val preferences = NotificationPreferences(
                        classReminders = classReminders,
                        paymentReminders = paymentReminders,
                        promotionalNotifications = promotionalNotifications,
                        bookingConfirmations = bookingConfirmations,
                        pushEnabled = pushEnabled,
                        emailEnabled = emailEnabled,
                        smsEnabled = smsEnabled
                    )
                    viewModel.updateNotificationPreferences(preferences)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !notificationSettingsState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiyaqaBrand
                )
            ) {
                if (notificationSettingsState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Save Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Clear messages when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearNotificationSettingsMessages()
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
private fun NotificationToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = LiyaqaBrand,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
