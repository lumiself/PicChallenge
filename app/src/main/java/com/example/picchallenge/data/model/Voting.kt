package com.example.picchallenge.data.model

import com.google.gson.annotations.SerializedName

/**
 * Voting system data models for WordPress API integration
 */

/**
 * Request model for voting
 */
data class VoteRequest(
    @SerializedName("email") val email: String
)

/**
 * Response model for vote submission
 */
data class VoteResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("new_vote_count") val newVoteCount: Int
)

/**
 * Voting eligibility check response
 */
data class VotingEligibility(
    @SerializedName("can_vote") val canVote: Boolean,
    @SerializedName("reason") val reason: String?,
    @SerializedName("message") val message: String
)

/**
 * Vote history item
 */
data class VoteHistory(
    @SerializedName("id") val id: Int,
    @SerializedName("photo_id") val photoId: Int,
    @SerializedName("photo_title") val photoTitle: String,
    @SerializedName("contest_id") val contestId: Int,
    @SerializedName("contest_name") val contestName: String,
    @SerializedName("vote_date") val voteDate: String
)

/**
 * Voting history response
 */
data class VotingHistoryResponse(
    @SerializedName("data") val data: List<VoteHistory>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("pages") val pages: Int
)

/**
 * Vote statistics response
 */
data class VoteStatsResponse(
    @SerializedName("total_votes") val totalVotes: Int,
    @SerializedName("total_photos") val totalPhotos: Int,
    @SerializedName("average_votes") val averageVotes: Double,
    @SerializedName("most_voted_photo") val mostVotedPhoto: Photo?,
    @SerializedName("least_voted_photo") val leastVotedPhoto: Photo?
)

/**
 * Photos with voting status for current user
 */
data class PhotosWithVotesResponse(
    @SerializedName("data") val data: List<PhotoWithVotes>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("pages") val pages: Int
)

/**
 * Photo with voting status information
 */
data class PhotoWithVotes(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("url") val url: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("medium") val medium: String,
    @SerializedName("large") val large: String,
    @SerializedName("votes") val votes: Int,
    @SerializedName("views") val views: Int,
    @SerializedName("author") val author: String,
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("contest_id") val contestId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("has_voted") val hasVoted: Boolean = false
)

/**
 * Vote statistics for analytics
 */
data class VoteStats(
    val totalVotes: Int,
    val totalPhotos: Int,
    val averageVotes: Double,
    val mostVotedPhoto: Photo?,
    val leastVotedPhoto: Photo?,
    val photosCount: Int
)

/**
 * Voting trend data for analytics
 */
data class VoteTrend(
    val date: String,
    val voteCount: Int
)

/**
 * Complete voting analytics data
 */
data class VoteAnalytics(
    val totalVotes: Int,
    val uniqueVoters: Int,
    val averageVotesPerPhoto: Double,
    val mostPopularPhoto: Photo?,
    val votingTrend: List<VoteTrend>,
    val hourlyDistribution: Map<Int, Int>
)
