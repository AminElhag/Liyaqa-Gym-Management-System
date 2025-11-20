package com.liyaqa.gym.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for monitoring network connectivity
 * Platform-specific implementations should be provided for Android and iOS
 */
interface ConnectivityMonitor {
    /**
     * Check if the device is currently connected to the internet
     */
    suspend fun isConnected(): Boolean

    /**
     * Observe connectivity changes as a flow
     */
    fun observeConnectivity(): Flow<Boolean>
}

/**
 * Default implementation that assumes always connected
 * Should be replaced with platform-specific implementations
 */
class DefaultConnectivityMonitor : ConnectivityMonitor {
    private val _connectivity = MutableStateFlow(true)

    override suspend fun isConnected(): Boolean = _connectivity.value

    override fun observeConnectivity(): StateFlow<Boolean> = _connectivity

    /**
     * Update connectivity status (for testing or manual updates)
     */
    fun setConnectivity(isConnected: Boolean) {
        _connectivity.value = isConnected
    }
}

/**
 * Extension function to check if the device is offline
 */
suspend fun ConnectivityMonitor.isOffline(): Boolean = !isConnected()
