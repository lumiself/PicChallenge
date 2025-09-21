package com.example.picchallenge.data.remote

import com.example.picchallenge.data.model.*
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface PhotoContestApiService {

    companion object {
        const val BASE_URL = "https://lumiself.co.zw/modeling/wp-json/photo-contest/v1/"
        const val JWT_BASE_URL = "https://lumiself.co.zw/modeling/wp-json/jwt-auth/v1/"
    }

    // ===== AUTHENTICATION ENDPOINTS =====
    @POST("token")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

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

    // ===== AUTHENTICATED ENDPOINTS =====
    @GET("wp-json/photo-contest/v1/user/photos")
    suspend fun getUserPhotos(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("contest_id") contestId: Int? = null
    ): Response<PhotoResponse>

    @Multipart
    @POST("wp-json/photo-contest/v1/photos/upload")
    suspend fun uploadPhoto(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
        @Part("contest_id") contestId: RequestBody,
        @Part("title") title: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("category_id") categoryId: RequestBody? = null,
        @Part("camera_model") cameraModel: RequestBody? = null
    ): Response<UploadResponse>

    @PUT("wp-json/photo-contest/v1/photos/{id}")
    suspend fun updatePhoto(
        @Header("Authorization") token: String,
        @Path("id") photoId: Int,
        @Body updateRequest: PhotoUpdateRequest
    ): Response<Photo>

    @DELETE("wp-json/photo-contest/v1/photos/{id}")
    suspend fun deletePhoto(
        @Header("Authorization") token: String,
        @Path("id") photoId: Int
    ): Response<SuccessResponse>

    @PUT("wp-json/photo-contest/v1/user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body profileUpdate: ProfileUpdateRequest
    ): Response<SuccessResponse>

    @Multipart
    @POST("wp-json/photo-contest/v1/user/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<AvatarUploadResponse>

    @PUT("wp-json/photo-contest/v1/user/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body passwordChange: PasswordChangeRequest
    ): Response<SuccessResponse>

    @GET("wp-json/photo-contest/v1/user/contests")
    suspend fun getUserContests(
        @Header("Authorization") token: String,
        @Query("status") status: String = "all"
    ): Response<List<Contest>>

    @GET("wp-json/photo-contest/v1/user/votes")
    suspend fun getUserVotingHistory(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int? = null,
        @Query("page") page: Int = 1
    ): Response<VoteHistoryResponse>

    // ===== ADMIN ENDPOINTS =====
    @POST("wp-json/photo-contest/v1/contests")
    suspend fun createContest(
        @Header("Authorization") token: String,
        @Body contestRequest: CreateContestRequest
    ): Response<CreateContestResponse>

    @PUT("wp-json/photo-contest/v1/contests/{id}")
    suspend fun updateContest(
        @Header("Authorization") token: String,
        @Path("id") contestId: Int,
        @Body contestRequest: CreateContestRequest
    ): Response<SuccessResponse>

    @DELETE("wp-json/photo-contest/v1/contests/{id}")
    suspend fun deleteContest(
        @Header("Authorization") token: String,
        @Path("id") contestId: Int
    ): Response<SuccessResponse>

    @GET("wp-json/photo-contest/v1/admin/photos")
    suspend fun getAdminPhotos(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("contest_id") contestId: Int? = null,
        @Query("status") status: String? = null,
        @Query("user_id") userId: Int? = null
    ): Response<AdminPhotoResponse>

    @PUT("wp-json/photo-contest/v1/admin/photos/{id}/approve")
    suspend fun approvePhoto(
        @Header("Authorization") token: String,
        @Path("id") photoId: Int
    ): Response<SuccessResponse>

    @PUT("wp-json/photo-contest/v1/admin/photos/{id}/reject")
    suspend fun rejectPhoto(
        @Header("Authorization") token: String,
        @Path("id") photoId: Int,
        @Body rejectRequest: RejectRequest
    ): Response<SuccessResponse>

    @GET("wp-json/photo-contest/v1/admin/votes")
    suspend fun getAdminVotes(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("contest_id") contestId: Int? = null,
        @Query("photo_id") photoId: Int? = null,
        @Query("user_id") userId: Int? = null
    ): Response<AdminVoteResponse>

    @GET("wp-json/photo-contest/v1/admin/votes/export")
    suspend fun exportVotes(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int,
        @Query("format") format: String = "json"
    ): Response<ResponseBody>

    @GET("wp-json/photo-contest/v1/admin/statistics")
    suspend fun getStatistics(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<StatisticsResponse>
}
