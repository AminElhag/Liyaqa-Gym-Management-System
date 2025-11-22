package com.liyaqa.android.ui.screens.classes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liyaqa.gym.domain.BookingStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toInstant

/**
 * Card displaying a class schedule
 */
@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun ClassScheduleCard(
    schedule: ScheduleWithClass,
    onClick: () -> Unit,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = kotlinx.datetime.LocalDateTime.Format { byUnicodePattern("h:mm a") }
    val startTime = timeFormat.format(schedule.schedule.startDateTime)
    val endTime = timeFormat.format(schedule.schedule.endDateTime)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Class name and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.schedule.instructorName ?: "Class",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$startTime - $endTime",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status badge
                BookingStatusBadge(schedule = schedule)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trainer info
            if (schedule.schedule.instructorName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Trainer",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = schedule.schedule.instructorName!!,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Duration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Duration",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = calculateDuration(schedule.schedule.startDateTime, schedule.schedule.endDateTime),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Capacity and booking button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Available spots
                CapacityIndicator(
                    availableSpots = schedule.schedule.availableSpots(),
                    totalCapacity = schedule.schedule.capacity,
                    isFull = schedule.schedule.isFull()
                )

                // Book button
                BookButton(
                    schedule = schedule,
                    onClick = onBookClick
                )
            }
        }
    }
}

/**
 * Booking status badge
 */
@Composable
private fun BookingStatusBadge(schedule: ScheduleWithClass) {
    val (text, containerColor, contentColor) = when {
        schedule.isBooked && schedule.booking != null -> {
            when (schedule.booking.status) {
                BookingStatus.CONFIRMED -> Triple(
                    "Booked",
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                BookingStatus.WAITLISTED -> Triple(
                    "Waitlisted",
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
                else -> return
            }
        }
        schedule.schedule.isCancelled -> Triple(
            "Cancelled",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        schedule.schedule.isFull() -> Triple(
            "Full",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        schedule.schedule.availableSpots() <= 3 -> Triple(
            "${schedule.schedule.availableSpots()} left",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> return
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Capacity indicator
 */
@Composable
private fun CapacityIndicator(
    availableSpots: Int,
    totalCapacity: Int,
    isFull: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isFull) Icons.Default.EventBusy else Icons.Default.People,
            contentDescription = "Capacity",
            modifier = Modifier.size(20.dp),
            tint = if (isFull) {
                MaterialTheme.colorScheme.error
            } else if (availableSpots <= 3) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$availableSpots / $totalCapacity spots",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isFull) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * Book button
 */
@Composable
private fun BookButton(
    schedule: ScheduleWithClass,
    onClick: () -> Unit
) {
    when {
        schedule.isBooked -> {
            OutlinedButton(
                onClick = onClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel")
            }
        }
        schedule.schedule.isCancelled -> {
            Button(
                onClick = {},
                enabled = false
            ) {
                Text("Cancelled")
            }
        }
        schedule.schedule.isFull() -> {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Queue,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Waitlist")
            }
        }
        else -> {
            Button(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.BookOnline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Book")
            }
        }
    }
}

/**
 * Calculate duration string
 */
private fun calculateDuration(
    start: kotlinx.datetime.LocalDateTime,
    end: kotlinx.datetime.LocalDateTime
): String {
    val startInstant = start.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault())
    val endInstant = end.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault())
    val duration = endInstant - startInstant
    val minutes = duration.inWholeMinutes.toInt()

    return when {
        minutes >= 60 -> {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            if (remainingMinutes > 0) {
                "${hours}h ${remainingMinutes}m"
            } else {
                "${hours}h"
            }
        }
        else -> "${minutes}m"
    }
}

/**
 * Trainer avatar placeholder
 */
@Composable
fun TrainerAvatar(
    trainerName: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = trainerName?.firstOrNull()?.uppercase() ?: "T",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
