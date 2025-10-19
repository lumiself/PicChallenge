package com.example.picchallenge.data.model

import com.google.gson.annotations.SerializedName

/**
 * Authentication request and response models for WordPress JWT authentication
 */

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user_email") val userEmail: String,
    @SerializedName("user_nicename") val userNicename: String,
    @SerializedName("user_display_name") val userDisplayName: String,
    @SerializedName("user_id") val userId: Int
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("registered") val registered: String
)

data class AuthErrorResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Map<String, Any>? = null
)

// Vote models are already defined in Photo.kt, so we don't need to redefine them here
