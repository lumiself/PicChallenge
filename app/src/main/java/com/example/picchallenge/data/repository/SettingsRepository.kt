package com.example.picchallenge.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "picchallenge_settings")

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context
) {
    companion object {
        private val BASE_URL_KEY = stringPreferencesKey("base_url")
        private const val DEFAULT_BASE_URL = "https://lumiself.co.zw/modeling/"
        private const val DEFAULT_JWT_BASE_URL = "https://lumiself.co.zw/modeling/wp-json/jwt-auth/v1/"
    }

    val baseUrl: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[BASE_URL_KEY] ?: DEFAULT_BASE_URL
        }

    val jwtBaseUrl: Flow<String> = baseUrl.map { url ->
        val cleanUrl = url.removeSuffix("/").removeSuffix("wp-json").removeSuffix("/")
        "$cleanUrl/wp-json/jwt-auth/v1/"
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[BASE_URL_KEY] = url
        }
    }

    suspend fun getCurrentBaseUrl(): String {
        return context.dataStore.data
            .map { preferences -> preferences[BASE_URL_KEY] ?: DEFAULT_BASE_URL }
            .first()
    }

    fun isValidUrl(url: String): Boolean {
        val trimmedUrl = url.trim()
        return trimmedUrl.isNotBlank() && 
               (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://") || !trimmedUrl.contains("://")) &&
               (trimmedUrl.contains(".") || trimmedUrl.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) && // Support IP addresses
               !trimmedUrl.endsWith(" ")
    }

    fun formatUrl(url: String): String {
        var formattedUrl = url.trim()
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "http://$formattedUrl"
        }
        if (!formattedUrl.endsWith("/")) {
            formattedUrl = "$formattedUrl/"
        }
        // Don't automatically add wp-json path - let each service handle its own API path
        return formattedUrl
    }
}
