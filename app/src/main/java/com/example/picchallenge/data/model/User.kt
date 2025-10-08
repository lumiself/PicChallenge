package com.example.picchallenge.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("avatar_url") val avatarUrl: String?
)


data class ProfileUpdateRequest(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("www") val website: String? = null,
    @SerializedName("fb_page") val facebookPage: String? = null,
    @SerializedName("twitter_page") val twitterPage: String? = null,
    @SerializedName("instagram_page") val instagramPage: String? = null
)


data class SuccessResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)


data class VoteHistoryResponse(
    @SerializedName("data") val data: List<VoteHistoryItem>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("pages") val pages: Int
)

data class VoteHistoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("photo_id") val photoId: Int,
    @SerializedName("photo_title") val photoTitle: String,
    @SerializedName("contest_id") val contestId: Int,
    @SerializedName("contest_name") val contestName: String,
    @SerializedName("vote_date") val voteDate: String,
    @SerializedName("email") val email: String
)

data class AdminPhotoResponse(
    @SerializedName("data") val data: List<AdminPhoto>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("pages") val pages: Int
)

data class AdminPhoto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("author") val author: String,
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("contest_id") val contestId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("rejection_reason") val rejectionReason: String?,
    @SerializedName("ip_address") val ipAddress: String
)

data class RejectRequest(
    @SerializedName("reason") val reason: String? = null
)

data class AdminVoteResponse(
    @SerializedName("data") val data: List<AdminVote>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("pages") val pages: Int
)

data class AdminVote(
    @SerializedName("id") val id: Int,
    @SerializedName("photo_id") val photoId: Int,
    @SerializedName("photo_title") val photoTitle: String,
    @SerializedName("contest_id") val contestId: Int,
    @SerializedName("contest_name") val contestName: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("vote_date") val voteDate: String,
    @SerializedName("email") val email: String,
    @SerializedName("ip_address") val ipAddress: String
)

data class StatisticsResponse(
    @SerializedName("votes") val votes: VoteStatistics,
    @SerializedName("photos") val photos: PhotoStatistics,
    @SerializedName("contests") val contests: ContestStatistics
)

data class VoteStatistics(
    @SerializedName("total_votes") val totalVotes: Int,
    @SerializedName("unique_voters") val uniqueVoters: Int,
    @SerializedName("voted_photos") val votedPhotos: Int,
    @SerializedName("contests_with_votes") val contestsWithVotes: Int
)

data class PhotoStatistics(
    @SerializedName("total_photos") val totalPhotos: Int,
    @SerializedName("unique_contributors") val uniqueContributors: Int,
    @SerializedName("contests_with_photos") val contestsWithPhotos: Int
)

data class ContestStatistics(
    @SerializedName("total_contests") val totalContests: Int,
    @SerializedName("active_contests") val activeContests: Int,
    @SerializedName("ended_contests") val endedContests: Int
)
