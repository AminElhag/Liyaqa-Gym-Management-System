package com.liyaqa.gym.network

import io.ktor.client.engine.*
import io.ktor.client.engine.android.*

/**
 * Android-specific HTTP engine implementation using OkHttp
 */
actual fun platformEngine(): HttpClientEngineFactory<*> = Android
