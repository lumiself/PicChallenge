package com.example.picchallenge.data.repository

import com.example.picchallenge.data.model.*
import com.example.picchallenge.data.remote.VotingApiService
import com.example.picchallenge.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling voting functionality with WordPress API
 * Manages voting eligibility, submission, and history with JWT authentication
 */
@Singleton
class VotingRepository @Inject constructor(
    private val votingApiService: VotingApiService,
    private val tokenManager: TokenManager
) {

    /**
     * Check if user can vote in a specific contest
     * Requires JWT authentication
     */
    suspend fun checkVotingEligibility(contestId: Int): Result<VotingEligibility> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAuthHeader()
                    ?: return@withContext Result.success(
                        VotingEligibility(
                            canVote = false,
                            reason = "not_authenticated",
                            message = "Please login to vote"
                        )
                    )

                val response = votingApiService.checkVotingEligibility(token, contestId)
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Authentication required. Please login."
                        403 -> "Access denied. You may not be eligible to vote."
                        404 -> "Contest not found."
                        else -> "Failed to check voting eligibility: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    /**
     * Submit a vote for a specific photo
     * Automatically checks eligibility and handles authentication
     */
    suspend fun submitVote(photoId: Int, contestId: Int): Result<VoteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get user token and data
                val token = tokenManager.getAuthHeader()
                    ?: throw Exception("User not logged in")
                
                val userEmail = tokenManager.getUserEmail()
                    ?: throw Exception("User email not found")

                // Check eligibility first
                val eligibility = checkVotingEligibility(contestId).getOrThrow()
                if (!eligibility.canVote) {
                    throw Exception(eligibility.message)
                }

                // Submit vote
                val voteRequest = VoteRequest(email = userEmail)
                val response = votingApiService.voteForPhoto(token, photoId, voteRequest)
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Authentication failed. Please login again."
                        403 -> "You are not allowed to vote for this photo."
                        404 -> "Photo not found."
                        409 -> "You have already voted for this photo."
                        else -> "Failed to submit vote: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Vote submission failed: ${e.message}"))
            }
        }
    }

    /**
     * Get user's voting history for specific contest or all contests
     */
    suspend fun getVotingHistory(contestId: Int? = null): Result<List<VoteHistory>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAuthHeader()
                    ?: throw Exception("User not logged in")

                val response = votingApiService.getVotingHistory(token, contestId)
                
                if (response.isSuccessful) {
                    response.body()?.let { historyResponse ->
                        Result.success(historyResponse.data)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Authentication required. Please login."
                        else -> "Failed to load voting history: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to load voting history: ${e.message}"))
            }
        }
    }

    /**
     * Get vote statistics for a contest
     */
    suspend fun getContestVoteStats(contestId: Int): Result<VoteStats> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAuthHeader()
                    ?: throw Exception("User not logged in")

                val response = votingApiService.getContestVoteStats(token, contestId)
                
                if (response.isSuccessful) {
                    response.body()?.let { statsResponse ->
                        val voteStats = VoteStats(
                            totalVotes = statsResponse.totalVotes,
                            totalPhotos = statsResponse.totalPhotos,
                            averageVotes = statsResponse.averageVotes,
                            mostVotedPhoto = statsResponse.mostVotedPhoto,
                            leastVotedPhoto = statsResponse.leastVotedPhoto,
                            photosCount = statsResponse.totalPhotos
                        )
                        Result.success(voteStats)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Authentication required. Please login."
                        404 -> "Contest not found."
                        else -> "Failed to load vote statistics: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to load vote statistics: ${e.message}"))
            }
        }
    }

    /**
     * Get photos with voting status for current user
     */
    suspend fun getContestPhotosWithVotes(contestId: Int, page: Int = 1, perPage: Int = 20): Result<List<PhotoWithVotes>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAuthHeader()
                val response = votingApiService.getContestPhotosWithVotes(token ?: "", contestId, page, perPage)
                
                if (response.isSuccessful) {
                    response.body()?.let { photosResponse ->
                        Result.success(photosResponse.data)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Authentication required. Please login to see voting status."
                        404 -> "Contest not found."
                        else -> "Failed to load photos with voting status: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to load photos with voting status: ${e.message}"))
            }
        }
    }

    /**
     * Check if user has already voted for a specific photo
     */
    suspend fun hasUserVotedForPhoto(photoId: Int, contestId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val votingHistory = getVotingHistory(contestId).getOrThrow()
                val hasVoted = votingHistory.any { it.photoId == photoId }
                Result.success(hasVoted)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to check voting history: ${e.message}"))
            }
        }
    }

    /**
     * Get comprehensive voting analytics for a contest
     */
    suspend fun getVotingAnalytics(contestId: Int): Result<VoteAnalytics> {
        return withContext(Dispatchers.IO) {
            try {
                // Get vote stats
                val voteStats = getContestVoteStats(contestId).getOrThrow()
                
                // Get voting history for unique voters count
                val votingHistory = getVotingHistory(contestId).getOrThrow()
                val uniqueVoters = votingHistory.map { it.id }.distinct().count()
                
                // Create analytics object
                val analytics = VoteAnalytics(
                    totalVotes = voteStats.totalVotes,
                    uniqueVoters = uniqueVoters,
                    averageVotesPerPhoto = voteStats.averageVotes,
                    mostPopularPhoto = voteStats.mostVotedPhoto,
                    votingTrend = emptyList(), // Would need additional API endpoint
                    hourlyDistribution = emptyMap() // Would need additional API endpoint
                )
                
                Result.success(analytics)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to generate voting analytics: ${e.message}"))
            }
        }
    }

    /**
     * Validate vote submission before sending to API
     */
    fun validateVoteSubmission(
        photoId: Int,
        contestId: Int,
        existingVotes: List<VoteHistory>
    ): VoteValidationResult {
        return when {
            // Check if user already voted for this photo
            existingVotes.any { it.photoId == photoId } -> {
                VoteValidationResult.Invalid("You have already voted for this photo")
            }
            
            // Additional validation rules can be added here
            else -> VoteValidationResult.Valid
        }
    }
}

/**
 * Vote validation result
 */
sealed class VoteValidationResult {
    object Valid : VoteValidationResult()
    data class Invalid(val reason: String) : VoteValidationResult()
}
