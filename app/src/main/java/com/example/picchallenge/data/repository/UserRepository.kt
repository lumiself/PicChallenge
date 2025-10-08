package com.example.picchallenge.data.repository

import com.example.picchallenge.data.model.User
import com.example.picchallenge.data.remote.PhotoContestApiService
import com.example.picchallenge.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: PhotoContestApiService
) {
    suspend fun getUser(userId: Int): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                // For now, return a mock user since we don't have user endpoints
                // In a real app, this would call the API
                NetworkResult.Success(
                    User(
                        id = userId,
                        username = "guest_user",
                        email = "guest@example.com",
                        displayName = "Guest User",
                        firstName = "Guest",
                        lastName = "User",
                        description = "Guest user account",
                        avatarUrl = null
                    )
                )
            } catch (e: Exception) {
                NetworkResult.Error("Network error: ${e.message}")
            }
        }
    }
}
