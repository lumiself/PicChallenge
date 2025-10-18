package com.example.picchallenge.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.voteDataStore: DataStore<Preferences> by preferencesDataStore(name = "vote_tracking")

/**
 * Data class to store vote information
 */
data class VoteRecord(
    val photoId: Int,
    val contestId: Int,
    val timestamp: Long = Date().time,
    val voteType: String = "vote" // "vote" or "rating"
)

/**
 * Repository to track voting history and enforce frequency limits
 */
@Singleton
class VoteTrackingRepository @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {
    companion object {
        private val VOTE_HISTORY_KEY = stringPreferencesKey("vote_history")
        private val RATING_HISTORY_KEY = stringPreferencesKey("rating_history")
        
        // Vote frequency constants based on WordPress plugin values
        const val VOTE_FREQUENCY_SINGLE = 0        // Only one single vote total
        const val VOTE_FREQUENCY_DAILY = 1         // Once per day
        const val VOTE_FREQUENCY_TWICE_DAILY = 2   // Twice per day (every 12 hours)
        const val VOTE_FREQUENCY_HOURLY = 3        // Once per hour
        const val VOTE_FREQUENCY_5_STAR = 4        // 5-star rating mode
        const val VOTE_FREQUENCY_10_STAR = 5       // 10-star rating mode
        const val VOTE_FREQUENCY_PER_PHOTO = 6     // Once per photo (like button)
    }

    /**
     * Get all vote history
     */
    private val voteHistoryFlow: Flow<List<VoteRecord>> = context.voteDataStore.data
        .map { preferences ->
            val json = preferences[VOTE_HISTORY_KEY] ?: "[]"
            try {
                gson.fromJson<List<VoteRecord>>(json, object : TypeToken<List<VoteRecord>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * Get all rating history
     */
    private val ratingHistoryFlow: Flow<List<VoteRecord>> = context.voteDataStore.data
        .map { preferences ->
            val json = preferences[RATING_HISTORY_KEY] ?: "[]"
            try {
                gson.fromJson<List<VoteRecord>>(json, object : TypeToken<List<VoteRecord>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * Check if user can vote based on contest frequency rules
     * Simplified implementation: Default to daily voting for all contests
     * Other voting frequencies will be implemented in a future update
     */
    suspend fun canVote(
        contestId: Int,
        photoId: Int,
        voteFrequency: Int,
        contestMode: Int = 1
    ): VoteEligibilityResult {
        val currentTime = Date().time
        val voteHistory = voteHistoryFlow.first()

        // For now, implement daily voting as the default for all vote frequencies
        // This ensures consistent behavior regardless of WordPress settings
        val lastVoteTime = voteHistory
            .filter { it.contestId == contestId }
            .maxOfOrNull { it.timestamp } ?: 0
        
        val hoursSinceLastVote = TimeUnit.MILLISECONDS.toHours(currentTime - lastVoteTime)
        
        return if (hoursSinceLastVote < 24 && lastVoteTime > 0) {
            val remainingHours = 24 - hoursSinceLastVote
            VoteEligibilityResult.NotAllowed(
                "You can vote again in $remainingHours hours.",
                remainingHours.toInt()
            )
        } else {
            VoteEligibilityResult.Allowed
        }
    }

    /**
     * Record a vote in the local history
     */
    suspend fun recordVote(photoId: Int, contestId: Int, voteType: String = "vote") {
        val currentHistory = voteHistoryFlow.first().toMutableList()
        val newVote = VoteRecord(photoId, contestId, System.currentTimeMillis(), voteType)
        currentHistory.add(newVote)
        
        context.voteDataStore.edit { preferences ->
            preferences[VOTE_HISTORY_KEY] = gson.toJson(currentHistory)
        }
    }

    /**
     * Clear vote history for a specific contest (useful for testing)
     */
    suspend fun clearContestVoteHistory(contestId: Int) {
        val currentHistory = voteHistoryFlow.first().toMutableList()
        currentHistory.removeAll { it.contestId == contestId }
        
        context.voteDataStore.edit { preferences ->
            preferences[VOTE_HISTORY_KEY] = gson.toJson(currentHistory)
        }
    }

    /**
     * Clear all vote history (useful for testing or user reset)
     */
    suspend fun clearAllVoteHistory() {
        context.voteDataStore.edit { preferences ->
            preferences[VOTE_HISTORY_KEY] = "[]"
            preferences[RATING_HISTORY_KEY] = "[]"
        }
    }

    /**
     * Get vote history for debugging
     */
    suspend fun getVoteHistory(): List<VoteRecord> {
        return voteHistoryFlow.first()
    }
}

/**
 * Result of voting eligibility check
 */
sealed class VoteEligibilityResult {
    object Allowed : VoteEligibilityResult()
    data class NotAllowed(
        val reason: String,
        val remainingHours: Int = 0,
        val remainingMinutes: Int = 0
    ) : VoteEligibilityResult()
}
