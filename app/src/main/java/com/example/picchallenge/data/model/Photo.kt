package com.example.picchallenge.data.model

import com.google.gson.annotations.SerializedName

data class Photo(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
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
    @SerializedName("category_id") val categoryId: Int
)

data class PhotoResponse(
    @SerializedName("data") val data: List<Photo>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("pages") val pages: Int
)

data class VoteRequest(
    @SerializedName("email") val email: String? = null
)

data class VoteResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("new_vote_count") val newVoteCount: Int
)

data class RatingRequest(
    @SerializedName("rating") val rating: Int
)

data class RatingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("new_average") val newAverage: Double,
    @SerializedName("total_votes") val totalVotes: Int
)
