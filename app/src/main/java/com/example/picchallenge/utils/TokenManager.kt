package com.example.picchallenge.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure token management using EncryptedSharedPreferences
 * Handles JWT token storage, retrieval, and validation
 */
@Singleton
class TokenManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "picchallenge_auth_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_DISPLAY_NAME = "user_display_name"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Save authentication token and user data securely
     */
    fun saveToken(token: String, userId: Int, userEmail: String, userDisplayName: String) {
        encryptedPrefs.edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_USER_DISPLAY_NAME, userDisplayName)
            apply()
        }
    }

    /**
     * Retrieve stored JWT token
     */
    fun getToken(): String? {
        return encryptedPrefs.getString(KEY_TOKEN, null)
    }

    /**
     * Retrieve stored user ID
     */
    fun getUserId(): Int {
        return encryptedPrefs.getInt(KEY_USER_ID, -1)
    }

    /**
     * Retrieve stored user email
     */
    fun getUserEmail(): String? {
        return encryptedPrefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Retrieve stored user display name
     */
    fun getUserDisplayName(): String? {
        return encryptedPrefs.getString(KEY_USER_DISPLAY_NAME, null)
    }

    /**
     * Check if user is authenticated (token exists and is valid)
     */
    fun isAuthenticated(): Boolean {
        val token = getToken()
        return !token.isNullOrEmpty() && isTokenValid(token)
    }

    /**
     * Basic token validation (checks if token exists and has proper format)
     * Note: For production, you should implement proper JWT token validation
     */
    private fun isTokenValid(token: String): Boolean {
        // Basic validation - check if token has JWT format (header.payload.signature)
        val parts = token.split(".")
        return parts.size == 3 && parts.all { it.isNotEmpty() }
    }

    /**
     * Clear all stored authentication data
     */
    fun clearToken() {
        encryptedPrefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_DISPLAY_NAME)
            apply()
        }
    }

    /**
     * Get authentication header value for API requests
     */
    fun getAuthHeader(): String? {
        return getToken()?.let { "Bearer $it" }
    }
}
