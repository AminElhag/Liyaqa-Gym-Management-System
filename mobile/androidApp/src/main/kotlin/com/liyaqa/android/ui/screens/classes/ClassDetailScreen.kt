package com.liyaqa.android.ui.screens.classes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

/**
 * Class detail screen showing full class information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    navController: NavController,
    viewModel: ClassDetailViewModel = hiltViewModel()
) {
    val classDetailState by viewModel.classDetailState.collectAsState()
    val bookingEvent by viewModel.bookingEvent.collectAsState()

    var showBookingDialog by remember { mutableStateOf(false) }

    // Handle booking events
    LaunchedEffect(bookingEvent) {
        when (bookingEvent) {
            is BookingEvent.Success -> {
                viewModel.clearBookingEvent()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Class Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = classDetailState) {
                is ClassDetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ClassDetailState.Success -> {
                    ClassDetailContent(
                        state = state,
                        onBookClick = { showBookingDialog = true }
                    )
                }

                is ClassDetailState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadClassDetail() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }

    // Booking confirmation dialog
    if (showBookingDialog && classDetailState is ClassDetailState.Success) {
        val state = classDetailState as ClassDetailState.Success
        val scheduleWithClass = ScheduleWithClass(
            schedule = state.schedule,
            gymClass = state.gymClass,
            isBooked = state.isBooked,
            booking = state.booking
        )

        BookingConfirmationDialog(
            schedule = scheduleWithClass,
            bookingEvent = bookingEvent,
            onConfirm = { viewModel.bookClass() },
            onCancel = {
                if (state.booking != null) {
                    viewModel.cancelBooking(state.booking.id)
                }
            },
            onDismiss = {
                showBookingDialog = false
                viewModel.clearBookingEvent()
            }
        )
    }
}

/**
 * Class detail content
 */
@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
private fun ClassDetailContent(
    state: ClassDetailState.Success,
    onBookClick: () -> Unit
) {
    val dateFormat = kotlinx.datetime.LocalDateTime.Format { byUnicodePattern("EEEE, MMMM d, yyyy") }
    val timeFormat = kotlinx.datetime.LocalDateTime.Format { byUnicodePattern("h:mm a") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header with class name and status
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = state.gymClass.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateFormat.format(state.schedule.startDateTime),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${timeFormat.format(state.schedule.startDateTime)} - ${
                        timeFormat.format(state.schedule.endDateTime)
                    }",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Booking status card
            if (state.isBooked && state.booking != null) {
                BookingStatusCard(booking = state.booking)
            }

            // Capacity card
            CapacityCard(schedule = state.schedule)

            // Trainer information
            if (state.schedule.instructorName != null) {
                TrainerCard(trainerName = state.schedule.instructorName!!)
            }

            // Class information
            ClassInfoCard(gymClass = state.gymClass, schedule = state.schedule)

            // Class description
            if (state.gymClass.description != null) {
                DescriptionCard(description = state.gymClass.description!!)
            }

            // Equipment needed (placeholder)
            EquipmentCard()

            // Difficulty level
            DifficultyCard(level = state.gymClass.level)

            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom button
        }

        // Bottom action button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                when {
                    state.isBooked -> {
                        Button(
                            onClick = onBookClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel Booking", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    state.schedule.isCancelled -> {
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = false
                        ) {
                            Text("Class Cancelled", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    state.schedule.isFull() -> {
                        Button(
                            onClick = onBookClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Queue,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Join Waitlist", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onBookClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookOnline,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Book This Class", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Booking status card
 */
@Composable
private fun BookingStatusCard(booking: com.liyaqa.gym.domain.Booking) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "You're Booked!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = booking.statusDisplayText(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Capacity card
 */
@Composable
private fun CapacityCard(schedule: com.liyaqa.gym.domain.ClassSchedule) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Capacity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { schedule.bookedCount.toFloat() / schedule.capacity },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${schedule.availableSpots()} spots available",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${schedule.bookedCount} / ${schedule.capacity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (schedule.waitlistCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${schedule.waitlistCount} on waitlist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Trainer card
 */
@Composable
private fun TrainerCard(trainerName: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrainerAvatar(trainerName = trainerName, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Trainer",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trainerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Class info card
 */
@Composable
private fun ClassInfoCard(
    gymClass: com.liyaqa.gym.domain.GymClass,
    schedule: com.liyaqa.gym.domain.ClassSchedule
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Class Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DetailRow(
                icon = Icons.Default.AccessTime,
                label = "Duration",
                value = gymClass.durationInHours()
            )

            DetailRow(
                icon = Icons.Default.FitnessCenter,
                label = "Type",
                value = gymClass.type.name.replace('_', ' ').lowercase()
                    .replaceFirstChar { it.uppercase() }
            )

            DetailRow(
                icon = Icons.Default.TrendingUp,
                label = "Level",
                value = gymClass.levelDescription()
            )

            gymClass.genderRestriction?.let { gender ->
                DetailRow(
                    icon = Icons.Default.Person,
                    label = "Gender Restriction",
                    value = gender.name.lowercase()
                        .replaceFirstChar { it.uppercase() }
                )
            }
        }
    }
}

/**
 * Description card
 */
@Composable
private fun DescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Equipment card
 */
@Composable
private fun EquipmentCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Equipment Needed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "All equipment provided by the gym",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Difficulty card
 */
@Composable
private fun DifficultyCard(level: com.liyaqa.gym.domain.ClassLevel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (level) {
                com.liyaqa.gym.domain.ClassLevel.BEGINNER -> MaterialTheme.colorScheme.tertiaryContainer
                com.liyaqa.gym.domain.ClassLevel.INTERMEDIATE -> MaterialTheme.colorScheme.secondaryContainer
                com.liyaqa.gym.domain.ClassLevel.ADVANCED -> MaterialTheme.colorScheme.errorContainer
                com.liyaqa.gym.domain.ClassLevel.ALL_LEVELS -> MaterialTheme.colorScheme.primaryContainer
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Difficulty Level",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = level.name.replace('_', ' ').lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Detail row component
 */
@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
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
