package com.liyaqa.android.ui.screens.classes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

/**
 * Class list screen showing available class schedules
 * Can be embedded in bottom navigation tabs (without Scaffold)
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClassListScreen(
    navController: NavController,
    viewModel: ClassListViewModel = hiltViewModel()
) {
    val classListState by viewModel.classListState.collectAsState()
    val bookingEvent by viewModel.bookingEvent.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showBookingDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<ScheduleWithClass?>(null) }

    // Handle booking events
    LaunchedEffect(bookingEvent) {
        when (bookingEvent) {
            is BookingEvent.Success -> {
                // Show success and clear
                viewModel.clearBookingEvent()
            }
            is BookingEvent.Error -> {
                // Error is shown in the dialog
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = classListState) {
            is ClassListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ClassListState.Success -> {
                ClassListContent(
                    state = state,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    onDateSelected = { date -> viewModel.selectDate(date) },
                    onFiltersChanged = { filters -> viewModel.updateFilters(filters) },
                    onScheduleClick = { schedule ->
                        navController.navigate("class/${schedule.schedule.id}")
                    },
                    onBookClick = { schedule ->
                        selectedSchedule = schedule
                        showBookingDialog = true
                    }
                )
            }

            is ClassListState.Error -> {
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
                        Button(onClick = { viewModel.loadSchedules() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }

    // Booking confirmation dialog
    if (showBookingDialog && selectedSchedule != null) {
        BookingConfirmationDialog(
            schedule = selectedSchedule!!,
            bookingEvent = bookingEvent,
            onConfirm = {
                viewModel.bookClass(selectedSchedule!!.schedule.id)
            },
            onCancel = {
                if (selectedSchedule!!.booking != null) {
                    viewModel.cancelBooking(selectedSchedule!!.booking!!.id)
                }
            },
            onDismiss = {
                showBookingDialog = false
                selectedSchedule = null
                viewModel.clearBookingEvent()
            }
        )
    }
}

/**
 * Content for class list when data is loaded
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClassListContent(
    state: ClassListState.Success,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onFiltersChanged: (ClassFilters) -> Unit,
    onScheduleClick: (ScheduleWithClass) -> Unit,
    onBookClick: (ScheduleWithClass) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Date selector
            item {
                DateSelector(
                    selectedDate = state.selectedDate,
                    onDateSelected = onDateSelected,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Filter chips
            item {
                FilterChipsRow(
                    filters = state.filters,
                    onFiltersChanged = onFiltersChanged,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Schedules grouped by time
            val groupedSchedules = state.schedules.groupByTimeOfDay()

            if (groupedSchedules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No classes available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                groupedSchedules.forEach { (timeLabel, schedules) ->
                    // Time group header
                    item {
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Schedules in this time group
                    items(schedules) { schedule ->
                        ClassScheduleCard(
                            schedule = schedule,
                            onClick = { onScheduleClick(schedule) },
                            onBookClick = { onBookClick(schedule) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * Date selector with horizontal scroll
 */
@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dates = remember {
        (0..13).map { today.plus(it, DateTimeUnit.DAY) }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dates.forEach { date ->
            DateChip(
                date = date,
                isSelected = date == selectedDate,
                isToday = date == today,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

/**
 * Individual date chip
 */
@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
private fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dayFormat = LocalDate.Format { byUnicodePattern("EEE") }
    val dateFormat = LocalDate.Format { byUnicodePattern("d") }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = dayFormat.format(date),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isToday) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null
    )
}

/**
 * Filter chips row
 */
@Composable
private fun FilterChipsRow(
    filters: ClassFilters,
    onFiltersChanged: (ClassFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Available only filter
        FilterChip(
            selected = filters.showOnlyAvailable,
            onClick = {
                onFiltersChanged(filters.copy(showOnlyAvailable = !filters.showOnlyAvailable))
            },
            label = { Text("Available Only") },
            leadingIcon = if (filters.showOnlyAvailable) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null
        )

        // Clear filters button
        if (filters.classType != null || filters.trainerId != null || filters.showOnlyAvailable) {
            FilterChip(
                selected = false,
                onClick = {
                    onFiltersChanged(ClassFilters())
                },
                label = { Text("Clear Filters") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

/**
 * Extension to group schedules by time of day
 */
private fun List<ScheduleWithClass>.groupByTimeOfDay(): Map<String, List<ScheduleWithClass>> {
    return this.groupBy { scheduleWithClass ->
        val hour = scheduleWithClass.schedule.startDateTime.hour
        when {
            hour < 12 -> "Morning (6:00 AM - 12:00 PM)"
            hour < 17 -> "Afternoon (12:00 PM - 5:00 PM)"
            else -> "Evening (5:00 PM - 10:00 PM)"
        }
    }.toSortedMap()
}
