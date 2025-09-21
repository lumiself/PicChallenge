package com.example.picchallenge.data.repository

import com.example.picchallenge.data.model.Contest
import com.example.picchallenge.data.model.ContestResponse
import com.example.picchallenge.data.model.CreateContestRequest
import com.example.picchallenge.data.model.CreateContestResponse
import com.example.picchallenge.data.model.SuccessResponse
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
        return try {
            val response = apiService.getContests(page, perPage, status)
            if (response.isSuccessful) {
                response.body()?.let { contestResponse ->
                    NetworkResult.Success(contestResponse)
                } ?: NetworkResult.Error("Empty response body")
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "WordPress API endpoint not found. Check your site URL and plugin installation."
                    401 -> "Authentication failed. Check JWT plugin configuration."
                    500 -> "Server error. Check WordPress logs for details."
                    else -> "API Error: ${response.code()} - ${response.message()}"
                }
                NetworkResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "Cannot connect to server. Check your URL and internet connection."
                is java.net.SocketTimeoutException -> "Connection timeout. Server may be down or slow."
                is java.net.ConnectException -> "Connection refused. Check if server is running."
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

    suspend fun getUserContests(
        token: String,
        status: String = "all"
    ): NetworkResult<List<Contest>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserContests(token, status)
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

    suspend fun createContest(
        token: String,
        contestRequest: CreateContestRequest
    ): NetworkResult<CreateContestResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createContest(token, contestRequest)
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

    suspend fun updateContest(
        token: String,
        contestId: Int,
        contestRequest: CreateContestRequest
    ): NetworkResult<SuccessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateContest(token, contestId, contestRequest)
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

    suspend fun deleteContest(
        token: String,
        contestId: Int
    ): NetworkResult<SuccessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteContest(token, contestId)
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
