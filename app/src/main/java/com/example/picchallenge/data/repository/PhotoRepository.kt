package com.example.picchallenge.data.repository

import com.example.picchallenge.data.model.*
import com.example.picchallenge.data.remote.PhotoContestApiService
import com.example.picchallenge.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val apiService: PhotoContestApiService
) {
    suspend fun getContestPhotos(
        contestId: Int,
        page: Int = 1,
        perPage: Int = 20,
        order: String = "date",
        categoryId: Int? = null
    ): NetworkResult<PhotoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getContestPhotos(contestId, page, perPage, order, categoryId)
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

    suspend fun getPopularPhotos(
        limit: Int = 20,
        timeframe: String = "all_time"
    ): NetworkResult<List<Photo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPopularPhotos(limit, timeframe)
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

    suspend fun getRecentPhotos(limit: Int = 20): NetworkResult<List<Photo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRecentPhotos(limit)
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

    suspend fun getPhotoDetails(photoId: Int): NetworkResult<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPhotoDetails(photoId)
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

    suspend fun votePhoto(photoId: Int, email: String? = null): NetworkResult<VoteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val voteRequest = VoteRequest(email)
                val response = apiService.votePhoto(photoId, voteRequest)
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

    suspend fun ratePhoto(photoId: Int, rating: Int): NetworkResult<RatingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val ratingRequest = RatingRequest(rating)
                val response = apiService.ratePhoto(photoId, ratingRequest)
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

    suspend fun getUserPhotos(
        token: String,
        page: Int = 1,
        contestId: Int? = null
    ): NetworkResult<PhotoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserPhotos(token, page, contestId)
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

    suspend fun uploadPhoto(
        token: String,
        imageFile: File,
        contestId: Int,
        title: String? = null,
        description: String? = null,
        categoryId: Int? = null,
        cameraModel: String? = null
    ): NetworkResult<UploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Create multipart body for image
                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                
                // Create request bodies for other parameters
                val contestIdBody = contestId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val titleBody = title?.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryIdBody = categoryId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val cameraModelBody = cameraModel?.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadPhoto(
                    token, imagePart, contestIdBody, titleBody, descriptionBody, categoryIdBody, cameraModelBody
                )
                
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

    suspend fun updatePhoto(
        token: String,
        photoId: Int,
        title: String? = null,
        description: String? = null,
        categoryId: Int? = null
    ): NetworkResult<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                val updateRequest = PhotoUpdateRequest(title, description, categoryId)
                val response = apiService.updatePhoto(token, photoId, updateRequest)
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

    suspend fun deletePhoto(token: String, photoId: Int): NetworkResult<SuccessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deletePhoto(token, photoId)
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

    suspend fun searchPhotos(
        query: String,
        contestId: Int? = null,
        categoryId: Int? = null
    ): NetworkResult<PhotoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchPhotos(query, contestId, categoryId)
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
