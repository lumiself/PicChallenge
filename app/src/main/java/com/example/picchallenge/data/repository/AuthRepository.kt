package com.example.picchallenge.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.picchallenge.data.model.LoginRequest
import com.example.picchallenge.data.model.LoginResponse
import com.example.picchallenge.data.remote.AuthApiService
import com.example.picchallenge.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val context: Context,
    private val authApiService: AuthApiService
) {
    companion object {
        private const val PREFS_NAME = "secure_auth_prefs"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_DISPLAY_NAME = "user_display_name"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val encryptedPrefs: SharedPreferences by lazy {
        createEncryptedSharedPreferences()
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun login(username: String, password: String): NetworkResult<LoginResponse> {
        return try {
            val loginRequest = LoginRequest(username, password)
            val response = authApiService.login(loginRequest)
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    saveAuthData(loginResponse)
                    _authState.value = AuthState.Authenticated(loginResponse)
                    NetworkResult.Success(loginResponse)
                } ?: NetworkResult.Error("Empty response body")
            } else {
                NetworkResult.Error("Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.message}")
        }
    }

    suspend fun logout() {
        clearAuthData()
        _authState.value = AuthState.Unauthenticated
    }

    fun getToken(): String? {
        return encryptedPrefs.getString(KEY_JWT_TOKEN, null)
    }

    fun getUserEmail(): String? {
        return encryptedPrefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserDisplayName(): String? {
        return encryptedPrefs.getString(KEY_USER_DISPLAY_NAME, null)
    }

    fun getUserId(): Int {
        return encryptedPrefs.getInt(KEY_USER_ID, -1)
    }

    fun isTokenExpired(): Boolean {
        val expiryTime = encryptedPrefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return if (expiryTime > 0) {
            System.currentTimeMillis() > expiryTime
        } else {
            true
        }
    }

    fun isAuthenticated(): Boolean {
        val token = getToken()
        return !token.isNullOrEmpty() && !isTokenExpired()
    }

    fun getAuthHeaders(): Map<String, String> {
        val token = getToken()
        return if (!token.isNullOrEmpty()) {
            mapOf("Authorization" to "Bearer $token")
        } else {
            emptyMap()
        }
    }

    suspend fun refreshTokenIfNeeded(): Boolean {
        return if (isTokenExpired()) {
            // Implement token refresh logic here
            // For now, logout user if token is expired
            logout()
            false
        } else {
            true
        }
    }

    private fun saveAuthData(loginResponse: LoginResponse) {
        encryptedPrefs.edit().apply {
            putString(KEY_JWT_TOKEN, loginResponse.token)
            putString(KEY_USER_EMAIL, loginResponse.userEmail)
            putString(KEY_USER_DISPLAY_NAME, loginResponse.userDisplayName)
            // Set token expiry to 24 hours from now (adjust based on your token TTL)
            putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (24 * 60 * 60 * 1000))
            apply()
        }
    }

    private fun clearAuthData() {
        encryptedPrefs.edit().apply {
            clear()
            apply()
        }
    }

    fun getCurrentAuthState(): AuthState {
        return if (isAuthenticated()) {
            val loginResponse = LoginResponse(
                token = getToken() ?: "",
                userEmail = getUserEmail() ?: "",
                userDisplayName = getUserDisplayName() ?: "",
                userNicename = ""
            )
            AuthState.Authenticated(loginResponse)
        } else {
            AuthState.Unauthenticated
        }
    }

    init {
        _authState.value = getCurrentAuthState()
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: LoginResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}
