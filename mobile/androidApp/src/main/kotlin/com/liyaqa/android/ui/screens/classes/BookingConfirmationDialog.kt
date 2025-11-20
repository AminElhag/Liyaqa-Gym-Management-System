package com.liyaqa.android.ui.screens.classes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

/**
 * Dialog for confirming class booking or cancellation
 */
@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun BookingConfirmationDialog(
    schedule: ScheduleWithClass,
    bookingEvent: BookingEvent,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = kotlinx.datetime.LocalDateTime.Format { byUnicodePattern("EEE, MMM d, yyyy") }
    val timeFormat = kotlinx.datetime.LocalDateTime.Format { byUnicodePattern("h:mm a") }

    var showCancellationReason by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (bookingEvent !is BookingEvent.Loading) {
                onDismiss()
            }
        },
        icon = {
            when (bookingEvent) {
                is BookingEvent.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                is BookingEvent.Success -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                is BookingEvent.Error -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Icon(
                        imageVector = if (schedule.isBooked) {
                            Icons.Default.Cancel
                        } else {
                            Icons.Default.BookOnline
                        },
                        contentDescription = null
                    )
                }
            }
        },
        title = {
            Text(
                text = when (bookingEvent) {
                    is BookingEvent.Success -> "Success!"
                    is BookingEvent.Error -> "Error"
                    is BookingEvent.Loading -> if (schedule.isBooked) "Cancelling..." else "Booking..."
                    else -> if (schedule.isBooked) "Cancel Booking" else "Confirm Booking"
                }
            )
        },
        text = {
            when (bookingEvent) {
                is BookingEvent.Success -> {
                    Text(bookingEvent.message)
                }
                is BookingEvent.Error -> {
                    Text(
                        bookingEvent.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (schedule.isBooked) {
                            CancellationInfo(schedule = schedule)
                        } else {
                            BookingInfo(
                                schedule = schedule,
                                dateFormat = dateFormat,
                                timeFormat = timeFormat
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (bookingEvent) {
                is BookingEvent.Success -> {
                    Button(onClick = onDismiss) {
                        Text("OK")
                    }
                }
                is BookingEvent.Error -> {
                    Button(onClick = onDismiss) {
                        Text("OK")
                    }
                }
                is BookingEvent.Loading -> {
                    // No button during loading
                }
                else -> {
                    if (schedule.isBooked) {
                        Button(
                            onClick = onCancel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Cancel Booking")
                        }
                    } else {
                        Button(onClick = onConfirm) {
                            Text(if (schedule.schedule.isFull()) "Join Waitlist" else "Confirm")
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (bookingEvent !is BookingEvent.Loading &&
                bookingEvent !is BookingEvent.Success &&
                bookingEvent !is BookingEvent.Error
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    )
}

/**
 * Booking information display
 */
@Composable
private fun BookingInfo(
    schedule: ScheduleWithClass,
    dateFormat: kotlinx.datetime.format.DateTimeFormat<kotlinx.datetime.LocalDateTime>,
    timeFormat: kotlinx.datetime.format.DateTimeFormat<kotlinx.datetime.LocalDateTime>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (schedule.schedule.isFull()) {
                "This class is full. You will be added to the waitlist."
            } else {
                "You are about to book the following class:"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Divider()

        // Class details
        InfoRow(
            icon = Icons.Default.FitnessCenter,
            label = "Class",
            value = schedule.schedule.instructorName ?: "Class"
        )

        InfoRow(
            icon = Icons.Default.CalendarToday,
            label = "Date",
            value = dateFormat.format(schedule.schedule.startDateTime)
        )

        InfoRow(
            icon = Icons.Default.AccessTime,
            label = "Time",
            value = "${timeFormat.format(schedule.schedule.startDateTime)} - ${
                timeFormat.format(schedule.schedule.endDateTime)
            }"
        )

        if (schedule.schedule.instructorName != null) {
            InfoRow(
                icon = Icons.Default.Person,
                label = "Trainer",
                value = schedule.schedule.instructorName!!
            )
        }

        InfoRow(
            icon = Icons.Default.People,
            label = "Available Spots",
            value = "${schedule.schedule.availableSpots()} / ${schedule.schedule.capacity}"
        )

        Divider()

        // Cancellation policy
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cancellation Policy",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = "You can cancel up to 2 hours before the class starts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Cancellation information display
 */
@Composable
private fun CancellationInfo(schedule: ScheduleWithClass) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Are you sure you want to cancel this booking?",
            style = MaterialTheme.typography.bodyMedium
        )

        if (schedule.booking != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Booking Status: ${schedule.booking.statusDisplayText()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    if (schedule.booking.waitlistPosition != null) {
                        Text(
                            text = "Position: #${schedule.booking.waitlistPosition}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Warning",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = "Late cancellations may affect your membership benefits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Info row with icon
 */
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
