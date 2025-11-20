package com.liyaqa.gym.network

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

/**
 * iOS-specific HTTP engine implementation using Darwin/NSURLSession
 */
actual fun platformEngine(): HttpClientEngineFactory<*> = Darwin
