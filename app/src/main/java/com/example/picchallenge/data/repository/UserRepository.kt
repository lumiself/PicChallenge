package com.example.picchallenge.data.repository

import com.example.picchallenge.data.model.*
import com.example.picchallenge.data.remote.PhotoContestApiService
import com.example.picchallenge.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: PhotoContestApiService
) {
    suspend fun login(username: String, password: String): NetworkResult<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val loginRequest = LoginRequest(username, password)
                val response = apiService.login(loginRequest)
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

    suspend fun updateProfile(
        token: String,
        profileUpdate: ProfileUpdateRequest
    ): NetworkResult<SuccessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateProfile(token, profileUpdate)
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

    suspend fun uploadAvatar(
        token: String,
        imageFile: File
    ): NetworkResult<AvatarUploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                
                val response = apiService.uploadAvatar(token, imagePart)
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

    suspend fun changePassword(
        token: String,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): NetworkResult<SuccessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val passwordChange = PasswordChangeRequest(currentPassword, newPassword, confirmPassword)
                val response = apiService.changePassword(token, passwordChange)
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

    suspend fun getUserVotingHistory(
        token: String,
        contestId: Int? = null,
        page: Int = 1
    ): NetworkResult<VoteHistoryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserVotingHistory(token, contestId, page)
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

    suspend fun getAdminPhotos(
        token: String,
        page: Int = 1,
        contestId: Int? = null,
        status: String? = null,
        userId: Int? = null
    ): NetworkResult<AdminPhotoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAdminPhotos(token, page, contestId, status, userId)
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

    suspend fun approvePhoto(
        token: String,
        photoId: Int
    ): NetworkResult<SuccessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.approvePhoto(token, photoId)
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

    suspend fun rejectPhoto(
        token: String,
        photoId: Int,
        reason: String? = null
    ): NetworkResult<SuccessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val rejectRequest = RejectRequest(reason)
                val response = apiService.rejectPhoto(token, photoId, rejectRequest)
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

    suspend fun getAdminVotes(
        token: String,
        page: Int = 1,
        contestId: Int? = null,
        photoId: Int? = null,
        userId: Int? = null
    ): NetworkResult<AdminVoteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAdminVotes(token, page, contestId, photoId, userId)
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

    suspend fun getStatistics(
        token: String,
        contestId: Int? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): NetworkResult<StatisticsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStatistics(token, contestId, dateFrom, dateTo)
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

    suspend fun exportVotes(
        token: String,
        contestId: Int,
        format: String = "json"
    ): NetworkResult<okhttp3.ResponseBody> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.exportVotes(token, contestId, format)
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
