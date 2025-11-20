package com.liyaqa.android.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

/**
 * Home screen with bottom navigation
 */
@Composable
fun HomeScreen(
    navController: NavHostController
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> DashboardTab()
                1 -> ClassesTab()
                2 -> BookingsTab()
                3 -> ProfileTab()
            }
        }
    }
}

@Composable
private fun DashboardTab() {
    Text(
        text = "Dashboard",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
private fun ClassesTab() {
    Text(
        text = "Classes",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
private fun BookingsTab() {
    Text(
        text = "My Bookings",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
private fun ProfileTab() {
    Text(
        text = "Profile",
        style = MaterialTheme.typography.headlineMedium
    )
}

private data class BottomNavItem(
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(Icons.Default.Home, "Home"),
    BottomNavItem(Icons.Default.FitnessCenter, "Classes"),
    BottomNavItem(Icons.Default.CalendarToday, "Bookings"),
    BottomNavItem(Icons.Default.AccountCircle, "Profile")
)
