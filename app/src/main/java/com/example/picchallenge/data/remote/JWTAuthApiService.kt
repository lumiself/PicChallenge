package com.example.picchallenge.data.remote

import com.example.picchallenge.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface JWTAuthApiService {
    
    /**
     * Login with username/email and password
     * Returns JWT token and user data
     */
    @POST("jwt-um/v1/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<JWTLoginResponse>

    /**
     * Register new user
     * No email verification required - instant login
     */
    @POST("jwt-um/v1/register")
    suspend fun registerUser(@Body request: JWTRegisterRequest): Response<JWTRegisterResponse>

    /**
     * Get current user profile (requires auth)
     */
    @GET("jwt-um/v1/user/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<JWTProfileResponse>

    /**
     * Check if user can vote in contest (requires auth)
     */
    @GET("jwt-um/v1/user/can-vote")
    suspend fun canUserVote(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int
    ): Response<CanVoteResponse>
}
