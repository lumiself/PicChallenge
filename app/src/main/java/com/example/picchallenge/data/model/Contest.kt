package com.example.picchallenge.data.model

import com.google.gson.annotations.SerializedName

data class Contest(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("vote_start_date") val voteStartDate: String,
    @SerializedName("register_end_date") val registerEndDate: String,
    @SerializedName("description") val description: String,
    @SerializedName("image_per_user") val imagePerUser: Int,
    @SerializedName("vote_frequency") val voteFrequency: Int,
    @SerializedName("gallery_layout") val galleryLayout: Int,
    @SerializedName("contest_mode") val contestMode: Int,
    @SerializedName("status") val status: String,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class ContestResponse(
    @SerializedName("data") val data: List<Contest>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("pages") val pages: Int
)

data class CreateContestRequest(
    @SerializedName("contest_name") val contestName: String,
    @SerializedName("contest_start") val contestStart: String,
    @SerializedName("contest_end") val contestEnd: String,
    @SerializedName("contest_vote_start") val contestVoteStart: String,
    @SerializedName("contest_register_end") val contestRegisterEnd: String,
    @SerializedName("contest_condition") val contestCondition: String? = null,
    @SerializedName("image_per_user") val imagePerUser: Int? = 5,
    @SerializedName("vote_frequency") val voteFrequency: Int? = 1,
    @SerializedName("gallery_layout") val galleryLayout: Int? = 1,
    @SerializedName("contest_mode") val contestMode: Int? = 1
)

data class CreateContestResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("contest_id") val contestId: Int
)

data class Category(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("contest_id") val contestId: Int
)
