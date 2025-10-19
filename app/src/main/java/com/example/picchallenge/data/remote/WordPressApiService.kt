package com.example.picchallenge.data.remote

import com.example.picchallenge.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface WordPressApiService {
    
    // Blog posts endpoints
    @GET("posts")
    suspend fun getPosts(
        @Query("categories") categoryId: Int? = null,
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1,
        @Query("_embed") embed: String = "true" // This will include featured media and author info
    ): Response<List<WordPressPost>>
    
    @GET("posts")
    suspend fun getPostsByCategory(
        @Query("categories") categoryId: Int,
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1,
        @Query("_embed") embed: String = "true"
    ): Response<List<WordPressPost>>

    // Authentication endpoints
    @POST("wp-json/jwt-auth/v1/token")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("wp-json/wp/v2/users/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<LoginResponse>

    // Voting endpoints
    @POST("wp-json/picchallenge/v1/vote/{contestantId}")
    suspend fun voteForContestant(
        @Path("contestantId") contestantId: Int,
        @Header("Authorization") token: String
    ): Response<VoteResponse>

    @GET("wp-json/picchallenge/v1/has-voted/{contestantId}")
    suspend fun hasUserVoted(
        @Path("contestantId") contestantId: Int,
        @Header("Authorization") token: String
    ): Response<Map<String, Boolean>>
}
