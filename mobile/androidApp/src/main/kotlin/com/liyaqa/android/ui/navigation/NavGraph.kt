package com.liyaqa.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.liyaqa.android.ui.screens.home.HomeScreen
import com.liyaqa.android.ui.screens.splash.SplashScreen

/**
 * Main navigation graph for the Liyaqa app
 */
@Composable
fun LiyaqaNavGraph(
    navController: NavHostController = rememberNavController(),
    deepLink: String? = null
) {
    // Handle deep links
    LaunchedEffect(deepLink) {
        deepLink?.let { link ->
            handleDeepLink(navController, link)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen (Main screen with bottom navigation)
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // TODO: Add more screens as they are implemented
        // - Login/Auth screens
        // - Class list and details
        // - Booking screens
        // - Profile screen
        // - Settings screen
        // - QR scanner screen
        // - Payment screens
    }
}

/**
 * Handle deep link navigation
 */
private fun handleDeepLink(navController: NavHostController, deepLink: String) {
    when {
        deepLink.startsWith("liyaqa://booking/") -> {
            val bookingId = deepLink.substringAfter("liyaqa://booking/")
            // Navigate to booking details
            // navController.navigate(Screen.BookingDetails.createRoute(bookingId))
        }
        deepLink.startsWith("liyaqa://class/") -> {
            val classId = deepLink.substringAfter("liyaqa://class/")
            // Navigate to class details
            // navController.navigate(Screen.ClassDetails.createRoute(classId))
        }
        deepLink.startsWith("liyaqa://promotion/") -> {
            val promotionId = deepLink.substringAfter("liyaqa://promotion/")
            // Navigate to promotion details
            // navController.navigate(Screen.Promotion.createRoute(promotionId))
        }
    }
}

/**
 * Sealed class representing all navigation screens
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ClassList : Screen("classes")
    data object ClassDetails : Screen("class/{classId}") {
        fun createRoute(classId: String) = "class/$classId"
    }
    data object Booking : Screen("booking")
    data object BookingDetails : Screen("booking/{bookingId}") {
        fun createRoute(bookingId: String) = "booking/$bookingId"
    }
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object QRScanner : Screen("qr-scanner")
    data object Payment : Screen("payment")
}
