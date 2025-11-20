package com.liyaqa.android.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.liyaqa.android.ui.navigation.Screen
import com.liyaqa.android.ui.theme.LiyaqaBrand
import com.liyaqa.gym.domain.Member

/**
 * Profile Screen
 * Displays user profile, membership card, and settings menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = profileState) {
                is ProfileState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ProfileState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadProfile() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is ProfileState.Success -> {
                    ProfileContent(
                        member = state.member,
                        subscription = state.subscription,
                        onEditProfile = {
                            navController.navigate("edit-profile")
                        },
                        onPaymentMethods = {
                            navController.navigate("payment-methods")
                        },
                        onInvoices = {
                            navController.navigate("invoices")
                        },
                        onNotificationSettings = {
                            navController.navigate("notification-settings")
                        },
                        onChangePassword = {
                            navController.navigate("change-password")
                        },
                        onSubscription = {
                            navController.navigate("subscription")
                        },
                        onLogout = {
                            showLogoutDialog = true
                        }
                    )
                }
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                            // Navigate to login and clear back stack
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileContent(
    member: Member,
    subscription: com.liyaqa.gym.domain.Subscription?,
    onEditProfile: () -> Unit,
    onPaymentMethods: () -> Unit,
    onInvoices: () -> Unit,
    onNotificationSettings: () -> Unit,
    onChangePassword: () -> Unit,
    onSubscription: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(member = member)
        }

        // Membership Card
        item {
            MembershipCard(
                subscription = subscription,
                memberId = member.id,
                onClick = onSubscription
            )
        }

        // Menu Items Section
        item {
            Text(
                text = "Account Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                subtitle = "Update your personal information",
                onClick = onEditProfile
            )
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.Payment,
                title = "Payment Methods",
                subtitle = "Manage payment methods",
                onClick = onPaymentMethods
            )
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.Receipt,
                title = "Invoices & Receipts",
                subtitle = "View payment history",
                onClick = onInvoices
            )
        }

        // Preferences Section
        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                title = "Notification Settings",
                subtitle = "Manage your notifications",
                onClick = onNotificationSettings
            )
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = "English",
                onClick = { /* TODO: Implement language picker */ }
            )
        }

        // Security Section
        item {
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.Lock,
                title = "Change Password",
                subtitle = "Update your password",
                onClick = onChangePassword
            )
        }

        // Logout
        item {
            ProfileMenuItem(
                icon = Icons.Default.ExitToApp,
                title = "Logout",
                subtitle = null,
                onClick = onLogout,
                textColor = MaterialTheme.colorScheme.error
            )
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Profile Header composable
 * Displays profile photo, name, and membership tier
 */
@Composable
private fun ProfileHeader(
    member: Member,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo
            if (member.profilePhotoUrl != null) {
                AsyncImage(
                    model = member.profilePhotoUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(LiyaqaBrand),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.name.first().uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Member Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = member.contactInfo.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (member.isActive()) LiyaqaBrand else MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = member.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Profile Menu Item composable
 */
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
