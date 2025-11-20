package com.liyaqa.android.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.liyaqa.android.ui.screens.classes.ClassListScreen

/**
 * Home screen with bottom navigation and dashboard content
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Home"
                            1 -> "Classes"
                            2 -> "My Bookings"
                            3 -> "Profile"
                            else -> "Liyaqa Gym"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> DashboardTab(
                    homeState = homeState,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    onCheckIn = { viewModel.checkIn() },
                    navController = navController
                )
                1 -> ClassesTab(navController = navController)
                2 -> BookingsTab()
                3 -> ProfileTab()
            }
        }
    }
}

/**
 * Dashboard tab with home content
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DashboardTab(
    homeState: HomeState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCheckIn: () -> Unit,
    navController: NavHostController
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
        when (homeState) {
            is HomeState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Welcome section
                    item {
                        WelcomeSection(member = homeState.member)
                    }

                    // Quick actions
                    item {
                        QuickActionsRow(
                            navController = navController,
                            onCheckIn = onCheckIn
                        )
                    }

                    // Upcoming bookings card
                    item {
                        UpcomingBookingsCard(
                            bookings = homeState.upcomingBookings,
                            navController = navController
                        )
                    }

                    // Activity summary
                    item {
                        ActivitySummaryCard(stats = homeState.activityStats)
                    }

                    // Notifications section
                    item {
                        NotificationsSection(
                            notifications = homeState.notifications,
                            navController = navController
                        )
                    }

                    // Featured content
                    item {
                        FeaturedContentSection()
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            is HomeState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${homeState.message}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Text("Retry")
                        }
                    }
                }
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
 * Classes tab with class list
 */
@Composable
private fun ClassesTab(navController: NavHostController) {
    // Embed the ClassListScreen directly in the tab
    // Remove the Scaffold from ClassListScreen to avoid nested app bars
    ClassListScreen(navController = navController)
}

/**
 * Bookings tab (placeholder)
 */
@Composable
private fun BookingsTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "My Bookings",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Profile tab (placeholder)
 */
@Composable
private fun ProfileTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Bottom navigation item data class
 */
private data class BottomNavItem(
    val icon: ImageVector,
    val label: String
)

/**
 * Bottom navigation items
 */
private val bottomNavItems = listOf(
    BottomNavItem(Icons.Default.Home, "Home"),
    BottomNavItem(Icons.Default.FitnessCenter, "Classes"),
    BottomNavItem(Icons.Default.CalendarToday, "Bookings"),
    BottomNavItem(Icons.Default.AccountCircle, "Profile")
)
