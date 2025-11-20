package com.liyaqa.android.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.liyaqa.gym.network.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of TokenStorage using EncryptedSharedPreferences
 * Provides secure storage for authentication tokens
 */
@Singleton
class AndroidTokenStorage @Inject constructor(
    private val context: Context
) : TokenStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveAccessToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }

    override suspend fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun saveRefreshToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_REFRESH_TOKEN, token)
            .apply()
    }

    override suspend fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRATION)
            .remove(KEY_MEMBER_ID)
            .apply()
    }

    override suspend fun saveTokenExpiration(expiresAt: Long) {
        sharedPreferences.edit()
            .putLong(KEY_TOKEN_EXPIRATION, expiresAt)
            .apply()
    }

    override suspend fun getTokenExpiration(): Long? {
        val expiration = sharedPreferences.getLong(KEY_TOKEN_EXPIRATION, -1L)
        return if (expiration != -1L) expiration else null
    }

    override suspend fun saveMemberId(memberId: String) {
        sharedPreferences.edit()
            .putString(KEY_MEMBER_ID, memberId)
            .apply()
    }

    override suspend fun getMemberId(): String? {
        return sharedPreferences.getString(KEY_MEMBER_ID, null)
    }

    companion object {
        private const val PREFS_NAME = "liyaqa_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRATION = "token_expiration"
        private const val KEY_MEMBER_ID = "member_id"
    }
}
