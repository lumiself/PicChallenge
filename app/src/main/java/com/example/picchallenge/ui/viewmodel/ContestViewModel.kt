package com.example.picchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchallenge.data.model.Contest
import com.example.picchallenge.data.model.ContestResponse
import com.example.picchallenge.data.model.CreateContestRequest
import com.example.picchallenge.data.model.CreateContestResponse
import com.example.picchallenge.data.model.SuccessResponse
import com.example.picchallenge.data.model.PhotoResponse
import com.example.picchallenge.data.model.Photo
import com.example.picchallenge.data.model.ImageDownloadState
import com.example.picchallenge.data.repository.ContestRepository
import com.example.picchallenge.utils.NetworkResult
import com.example.picchallenge.utils.ImageDownloadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContestViewModel @Inject constructor(
    private val contestRepository: ContestRepository,
    private val imageDownloadManager: ImageDownloadManager
) : ViewModel() {

    private val _contests = MutableStateFlow<NetworkResult<ContestResponse>>(NetworkResult.Loading)
    val contests: StateFlow<NetworkResult<ContestResponse>> = _contests.asStateFlow()

    private val _contestDetails = MutableStateFlow<NetworkResult<Contest>?>(null)
    val contestDetails: StateFlow<NetworkResult<Contest>?> = _contestDetails.asStateFlow()

    private val _userContests = MutableStateFlow<NetworkResult<List<Contest>>>(NetworkResult.Loading)
    val userContests: StateFlow<NetworkResult<List<Contest>>> = _userContests.asStateFlow()

    private val _contestPhotos = MutableStateFlow<NetworkResult<PhotoResponse>>(NetworkResult.Loading)
    val contestPhotos: StateFlow<NetworkResult<PhotoResponse>> = _contestPhotos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Image download states for different contests
    private val _imageDownloadStates = mutableMapOf<Int, StateFlow<ImageDownloadState>>()

    /**
     * Get the image download state for a specific contest
     */
    fun getImageDownloadState(contestId: Int): StateFlow<ImageDownloadState>? {
        return _imageDownloadStates[contestId]
    }

    /**
     * Start downloading images for a contest when photos are loaded
     */
    fun startImageDownload(contestId: Int, photos: List<Photo>) {
        if (_imageDownloadStates[contestId] == null) {
            val downloadState = imageDownloadManager.startDownloadingContestImages(contestId, photos)
            _imageDownloadStates[contestId] = downloadState
        }
    }

    /**
     * Get cached image path if available, otherwise return original URL
     */
    fun getCachedImageUrl(originalUrl: String): String {
        return imageDownloadManager.getCachedImagePath(originalUrl) ?: originalUrl
    }

    fun loadContests(
        page: Int = 1,
        perPage: Int = 20,
        status: String = "all"
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _contests.value = NetworkResult.Loading
                val result = contestRepository.getContests(page, perPage, status)
                _contests.value = result
            } catch (e: Exception) {
                // Handle any exceptions that occur during the API call
                _contests.value = NetworkResult.Error(
                    message = "Network error: ${e.message ?: "Unknown error occurred"}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadContestDetails(contestId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _contestDetails.value = NetworkResult.Loading
            val result = contestRepository.getContestDetails(contestId)
            _contestDetails.value = result
            _isLoading.value = false
        }
    }

    fun loadContestPhotos(contestId: Int, page: Int = 1, perPage: Int = 20) {
        viewModelScope.launch {
            try {
                _contestPhotos.value = NetworkResult.Loading
                val result = contestRepository.getContestPhotos(contestId, page, perPage)
                _contestPhotos.value = result
            } catch (e: Exception) {
                _contestPhotos.value = NetworkResult.Error(
                    message = "Failed to load contest photos: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun loadUserContests(token: String, status: String = "all") {
        viewModelScope.launch {
            _isLoading.value = true
            _userContests.value = NetworkResult.Loading
            val result = contestRepository.getUserContests(token, status)
            _userContests.value = result
            _isLoading.value = false
        }
    }

    fun createContest(token: String, contestRequest: CreateContestRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = contestRepository.createContest(token, contestRequest)
            // Handle result - could emit to a separate state flow for creation results
            _isLoading.value = false
        }
    }

    fun updateContest(token: String, contestId: Int, contestRequest: CreateContestRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = contestRepository.updateContest(token, contestId, contestRequest)
            // Handle result - could emit to a separate state flow for update results
            _isLoading.value = false
        }
    }

    fun deleteContest(token: String, contestId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = contestRepository.deleteContest(token, contestId)
            // Handle result - could emit to a separate state flow for deletion results
            _isLoading.value = false
        }
    }

    fun refreshContests() {
        loadContests()
    }
}
