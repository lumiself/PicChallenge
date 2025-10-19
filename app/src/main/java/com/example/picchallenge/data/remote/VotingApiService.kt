package com.example.picchallenge.data.remote

import com.example.picchallenge.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for WordPress voting system
 * Handles voting endpoints with JWT authentication
 */
interface VotingApiService {
    
    /**
     * Check if user can vote in a specific contest
     * Requires JWT authentication
     */
    @GET("jwt-um/v1/user/can-vote")
    suspend fun checkVotingEligibility(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int
    ): Response<VotingEligibility>
    
    /**
     * Vote for a specific photo
     * Requires JWT authentication
     */
    @POST("photo-contest/v1/photos/{id}/vote")
    suspend fun voteForPhoto(
        @Header("Authorization") token: String,
        @Path("id") photoId: Int,
        @Body voteRequest: VoteRequest
    ): Response<VoteResponse>
    
    /**
     * Get user's voting history
     * Requires JWT authentication
     */
    @GET("jwt-um/v1/user/voting-history")
    suspend fun getVotingHistory(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<VotingHistoryResponse>
    
    /**
     * Get vote statistics for a contest
     * Requires JWT authentication
     */
    @GET("photo-contest/v1/contests/{id}/vote-stats")
    suspend fun getContestVoteStats(
        @Header("Authorization") token: String,
        @Path("id") contestId: Int
    ): Response<VoteStatsResponse>
    
    /**
     * Get photos with voting status for current user
     * Requires JWT authentication
     */
    @GET("photo-contest/v1/contests/{id}/photos-with-votes")
    suspend fun getContestPhotosWithVotes(
        @Header("Authorization") token: String,
        @Path("id") contestId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<PhotosWithVotesResponse>
}
