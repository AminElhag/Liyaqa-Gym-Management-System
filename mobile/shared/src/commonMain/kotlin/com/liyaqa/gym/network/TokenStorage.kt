package com.liyaqa.gym.network

/**
 * Interface for storing and retrieving authentication tokens
 */
interface TokenStorage {
    /**
     * Save the access token
     */
    suspend fun saveAccessToken(token: String)

    /**
     * Get the current access token
     */
    suspend fun getAccessToken(): String?

    /**
     * Save the refresh token
     */
    suspend fun saveRefreshToken(token: String)

    /**
     * Get the current refresh token
     */
    suspend fun getRefreshToken(): String?

    /**
     * Clear all tokens (on logout)
     */
    suspend fun clearTokens()

    /**
     * Save token expiration time in milliseconds
     */
    suspend fun saveTokenExpiration(expiresAt: Long)

    /**
     * Get token expiration time in milliseconds
     */
    suspend fun getTokenExpiration(): Long?

    /**
     * Check if the access token is expired
     */
    suspend fun isTokenExpired(): Boolean {
        val expiresAt = getTokenExpiration() ?: return true
        return System.currentTimeMillis() >= expiresAt
    }
}

/**
 * In-memory implementation of TokenStorage for testing
 */
class InMemoryTokenStorage : TokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var tokenExpiration: Long? = null

    override suspend fun saveAccessToken(token: String) {
        accessToken = token
    }

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun saveRefreshToken(token: String) {
        refreshToken = token
    }

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
        tokenExpiration = null
    }

    override suspend fun saveTokenExpiration(expiresAt: Long) {
        tokenExpiration = expiresAt
    }

    override suspend fun getTokenExpiration(): Long? = tokenExpiration
}
