package com.example.picchallenge.data.model

import com.google.gson.annotations.SerializedName

/**
 * Authentication request and response models for WordPress JWT authentication
 */

// Login Request (same as before)
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// NEW: JWT Login Response
data class JWTLoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: JWTUser,
    @SerializedName("message") val message: String
)

data class JWTUser(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("avatar") val avatar: String?
)

// NEW: JWT Register Request (same as before, but response is different)
data class JWTRegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("first_name") val firstName: String = "",
    @SerializedName("last_name") val lastName: String = ""
)

// NEW: JWT Register Response
data class JWTRegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("message") val message: String,
    @SerializedName("verification_required") val verificationRequired: Boolean
)

// NEW: Profile Response
data class JWTProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("user") val user: JWTUser
)

// NEW: Can Vote Response
data class CanVoteResponse(
    @SerializedName("can_vote") val canVote: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("reason") val reason: String? = null
)

// Legacy models (keep for backward compatibility during transition)
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
