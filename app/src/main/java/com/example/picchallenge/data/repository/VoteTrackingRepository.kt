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
     * Enhanced implementation: Tracks voting per photo (contestant) with 24-hour restriction
     */
    suspend fun canVote(
        contestId: Int,
        photoId: Int,
        voteFrequency: Int,
        contestMode: Int = 1
    ): VoteEligibilityResult {
        val currentTime = Date().time
        val voteHistory = voteHistoryFlow.first()

        // Debug logging to check if data is being persisted
        println("DEBUG: VoteTrackingRepository.canVote() - Checking photo $photoId in contest $contestId")
        println("DEBUG: Current vote history size: ${voteHistory.size}")
        println("DEBUG: Vote history for photo $photoId: ${voteHistory.filter { it.photoId == photoId }}")

        // Track voting per individual photo (contestant) with 24-hour restriction
        val photoVotes = voteHistory.filter { it.photoId == photoId }
        
        if (photoVotes.isEmpty()) {
            println("DEBUG: No votes found for photo $photoId - allowing vote")
            return VoteEligibilityResult.Allowed
        }
        
        val lastVoteTime = photoVotes.maxOf { it.timestamp }
        val timeSinceLastVote = currentTime - lastVoteTime
        val hoursSinceLastVote = TimeUnit.MILLISECONDS.toHours(timeSinceLastVote)
        val minutesSinceLastVote = TimeUnit.MILLISECONDS.toMinutes(timeSinceLastVote) % 60
        
        println("DEBUG: Last vote time: $lastVoteTime, Hours since last vote: $hoursSinceLastVote")
        
        return if (hoursSinceLastVote < 24) {
            val remainingHours = 23 - hoursSinceLastVote
            val remainingMinutes = 59 - minutesSinceLastVote
            val remainingMillis = (24 * 60 * 60 * 1000) - timeSinceLastVote
            
            println("DEBUG: Vote not allowed - remaining hours: $remainingHours, minutes: $remainingMinutes")
            
            // Format the remaining time as "23h 59m left"
            val timeString = formatRemainingTime(remainingMillis)
            
            VoteEligibilityResult.NotAllowed(
                reason = "$timeString left",
                remainingHours = remainingHours.toInt(),
                remainingMinutes = remainingMinutes.toInt()
            )
        } else {
            println("DEBUG: Vote allowed - 24 hours have passed")
            VoteEligibilityResult.Allowed
        }
    }

    /**
     * Format remaining time in milliseconds to "Xh Ym" format
     */
    private fun formatRemainingTime(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        return "${hours}h ${minutes}m"
    }

    /**
     * Record a vote in the local history
     */
    suspend fun recordVote(photoId: Int, contestId: Int, voteType: String = "vote") {
        val currentHistory = voteHistoryFlow.first().toMutableList()
        val newVote = VoteRecord(photoId, contestId, System.currentTimeMillis(), voteType)
        currentHistory.add(newVote)
        
        println("DEBUG: Recording vote - photoId: $photoId, contestId: $contestId, timestamp: ${newVote.timestamp}")
        println("DEBUG: New history size: ${currentHistory.size}")
        
        context.voteDataStore.edit { preferences ->
            preferences[VOTE_HISTORY_KEY] = gson.toJson(currentHistory)
        }
        
        // Verify the vote was recorded by reading it back immediately
        val updatedHistory = voteHistoryFlow.first()
        println("DEBUG: Verified recorded vote - updated history size: ${updatedHistory.size}")
        println("DEBUG: Latest vote in history: ${updatedHistory.lastOrNull()}")
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

    /**
     * Get debug information about vote persistence
     */
    suspend fun getDebugInfo(): String {
        val history = voteHistoryFlow.first()
        val currentTime = Date().time
        
        val debugInfo = buildString {
            appendLine("=== VOTE TRACKING DEBUG INFO ===")
            appendLine("Total votes in history: ${history.size}")
            appendLine("Current time: $currentTime")
            
            if (history.isNotEmpty()) {
                appendLine("\nRecent votes:")
                history.takeLast(5).forEach { vote ->
                    val hoursAgo = TimeUnit.MILLISECONDS.toHours(currentTime - vote.timestamp)
                    appendLine("  Photo ${vote.photoId} in Contest ${vote.contestId}: ${hoursAgo}h ago")
                }
                
                appendLine("\nAll votes by photo:")
                history.groupBy { it.photoId }.forEach { (photoId, votes) ->
                    val lastVote = votes.maxBy { it.timestamp }
                    val hoursAgo = TimeUnit.MILLISECONDS.toHours(currentTime - lastVote.timestamp)
                    appendLine("  Photo $photoId: Last vote ${hoursAgo}h ago")
                }
            } else {
                appendLine("\nNo votes found in history")
            }
            appendLine("================================")
        }
        
        return debugInfo
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
