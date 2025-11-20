package com.liyaqa.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.liyaqa.android.ui.navigation.LiyaqaNavGraph
import com.liyaqa.android.ui.theme.LiyaqaTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity using Single Activity Architecture with Jetpack Compose
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            LiyaqaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LiyaqaNavGraph(
                        deepLink = intent.getStringExtra("deep_link")
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Handle deep link from intent
        intent.getStringExtra("deep_link")?.let { deepLink ->
            // Navigation will be handled in the NavGraph
            android.util.Log.d("MainActivity", "Deep link: $deepLink")
        }
    }
}
