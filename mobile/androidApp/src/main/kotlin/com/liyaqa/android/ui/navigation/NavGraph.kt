package com.liyaqa.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.liyaqa.android.ui.screens.auth.LoginScreen
import com.liyaqa.android.ui.screens.auth.RegisterScreen
import com.liyaqa.android.ui.screens.classes.ClassDetailScreen
import com.liyaqa.android.ui.screens.classes.ClassListScreen
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

        // Login Screen
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // Register Screen
        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        // Home Screen (Main screen with bottom navigation)
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // Class List Screen
        composable(route = Screen.ClassList.route) {
            ClassListScreen(navController = navController)
        }

        // Class Detail Screen
        composable(
            route = Screen.ClassDetails.route,
            arguments = listOf(
                navArgument("scheduleId") {
                    type = NavType.StringType
                }
            )
        ) {
            ClassDetailScreen(navController = navController)
        }

        // TODO: Add more screens as they are implemented
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
            val scheduleId = deepLink.substringAfter("liyaqa://class/")
            // Navigate to class details
            navController.navigate(Screen.ClassDetails.createRoute(scheduleId))
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
    data object ClassDetails : Screen("class/{scheduleId}") {
        fun createRoute(scheduleId: String) = "class/$scheduleId"
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
