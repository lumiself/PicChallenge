package com.example.picchallenge.data.remote

import com.example.picchallenge.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface PhotoContestApiService {

    companion object {
        const val BASE_URL = "https://lumiself.co.zw/modeling/wp-json/photo-contest/v1/"
        const val JWT_BASE_URL = "https://lumiself.co.zw/modeling/wp-json/jwt-auth/v1/"
    }


    // ===== PUBLIC ENDPOINTS =====
    @GET("contests")
    suspend fun getContests(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("status") status: String = "active"
    ): Response<ContestResponse>

    @GET("contests/{id}")
    suspend fun getContestDetails(
        @Path("id") contestId: Int
    ): Response<Contest>

    @GET("contests/{id}/photos")
    suspend fun getContestPhotos(
        @Path("id") contestId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("order") order: String = "date",
        @Query("category") categoryId: Int? = null
    ): Response<PhotoResponse>

    @GET("photos/popular")
    suspend fun getPopularPhotos(
        @Query("limit") limit: Int = 20,
        @Query("timeframe") timeframe: String = "all_time"
    ): Response<List<Photo>>

    @GET("photos/recent")
    suspend fun getRecentPhotos(
        @Query("limit") limit: Int = 20
    ): Response<List<Photo>>

    @GET("photos/{id}")
    suspend fun getPhotoDetails(
        @Path("id") photoId: Int
    ): Response<Photo>

    @POST("photos/{id}/vote")
    suspend fun votePhoto(
        @Path("id") photoId: Int,
        @Body voteRequest: VoteRequest
    ): Response<VoteResponse>

    @POST("photos/{id}/rate")
    suspend fun ratePhoto(
        @Path("id") photoId: Int,
        @Body ratingRequest: RatingRequest
    ): Response<RatingResponse>

    @GET("categories")
    suspend fun getCategories(
        @Query("contest_id") contestId: Int? = null
    ): Response<List<Category>>

    @GET("search")
    suspend fun searchPhotos(
        @Query("q") query: String,
        @Query("contest_id") contestId: Int? = null,
        @Query("category") categoryId: Int? = null
    ): Response<PhotoResponse>


}
