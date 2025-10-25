package com.example.picchallenge.data.repository

import com.example.picchallenge.data.model.Contest
import com.example.picchallenge.data.model.ContestResponse
import com.example.picchallenge.data.remote.PhotoContestApiService
import com.example.picchallenge.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContestRepository @Inject constructor(
    private val apiService: PhotoContestApiService
) {
    suspend fun getContests(
        page: Int = 1,
        perPage: Int = 20,
        status: String = "active"
    ): NetworkResult<ContestResponse> {
        println("DEBUG: ContestRepository - getContests() called with page: $page, perPage: $perPage, status: $status")
        return try {
            println("DEBUG: ContestRepository - Calling apiService.getContests()")
            val response = apiService.getContests(page, perPage, status)
            println("DEBUG: ContestRepository - API response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful) {
                println("DEBUG: ContestRepository - Response successful, checking body")
                response.body()?.let { contestResponse ->
                    println("DEBUG: ContestRepository - Response body received with ${contestResponse.data.size} contests")
                    NetworkResult.Success(contestResponse)
                } ?: run {
                    println("DEBUG: ContestRepository - Response body is null")
                    NetworkResult.Error("Empty response body")
                }
            } else {
                println("DEBUG: ContestRepository - Response failed with code: ${response.code()}")
                val errorMessage = when (response.code()) {
                    404 -> "WordPress API endpoint not found. Check your site URL and plugin installation."
                    401 -> "Authentication failed. Check JWT plugin configuration."
                    500 -> "Server error. Check WordPress logs for details."
                    else -> "API Error: ${response.code()} - ${response.message()}"
                }
                NetworkResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            println("DEBUG: ContestRepository - Exception caught: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "Cannot connect to server. Check your internet connection."
                is java.net.SocketTimeoutException -> "Connection timeout. Server may be down or slow."
                is java.net.ConnectException -> "Connection refused. Server may be unavailable."
                else -> "Network error: ${e.message ?: "Unknown error"}"
            }
            NetworkResult.Error(errorMessage)
        }
    }

    suspend fun getContestDetails(contestId: Int): NetworkResult<Contest> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getContestDetails(contestId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        NetworkResult.Success(it)
                    } ?: NetworkResult.Error("Empty response body")
                } else {
                    NetworkResult.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Network error: ${e.message}")
            }
        }
    }

    suspend fun getContestPhotos(
        contestId: Int,
        page: Int = 1,
        perPage: Int = 20,
        order: String = "date"
    ): NetworkResult<com.example.picchallenge.data.model.PhotoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getContestPhotos(contestId, page, perPage, order)
                if (response.isSuccessful) {
                    response.body()?.let {
                        NetworkResult.Success(it)
                    } ?: NetworkResult.Error("Empty response body")
                } else {
                    NetworkResult.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Network error: ${e.message}")
            }
        }
    }
}
