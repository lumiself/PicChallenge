package com.example.picchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchallenge.data.model.Contest
import com.example.picchallenge.data.model.ContestResponse
import com.example.picchallenge.data.model.PhotoResponse
import com.example.picchallenge.data.model.Photo
import com.example.picchallenge.data.model.ImageDownloadState
import com.example.picchallenge.data.repository.ContestRepository
import com.example.picchallenge.data.repository.PhotoRepository
import com.example.picchallenge.data.model.VoteResponse
import com.example.picchallenge.utils.NetworkResult
import com.example.picchallenge.utils.ImageDownloadManager
import com.example.picchallenge.data.repository.VoteTrackingRepository
import com.example.picchallenge.data.repository.VoteEligibilityResult
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
    private val photoRepository: PhotoRepository,
    private val imageDownloadManager: ImageDownloadManager,
    private val voteTrackingRepository: VoteTrackingRepository
) : ViewModel() {

    private val _contests = MutableStateFlow<NetworkResult<ContestResponse>>(NetworkResult.Loading)
    val contests: StateFlow<NetworkResult<ContestResponse>> = _contests.asStateFlow()

    private val _contestDetails = MutableStateFlow<NetworkResult<Contest>?>(null)
    val contestDetails: StateFlow<NetworkResult<Contest>?> = _contestDetails.asStateFlow()


    private val _contestPhotos = MutableStateFlow<NetworkResult<PhotoResponse>>(NetworkResult.Loading)
    val contestPhotos: StateFlow<NetworkResult<PhotoResponse>> = _contestPhotos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Vote state management
    private val _voteResult = MutableStateFlow<NetworkResult<VoteResponse>?>(null)
    val voteResult: StateFlow<NetworkResult<VoteResponse>?> = _voteResult.asStateFlow()
    
    private val _votingPhotos = MutableStateFlow<Set<Int>>(emptySet())
    val votingPhotos: StateFlow<Set<Int>> = _votingPhotos.asStateFlow()

    // Voting eligibility state
    private val _voteEligibility = MutableStateFlow<Map<Int, VoteEligibilityResult>>(emptyMap())
    val voteEligibility: StateFlow<Map<Int, VoteEligibilityResult>> = _voteEligibility.asStateFlow()

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
        status: String = "all",
        isRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Only set to Loading state if it's not a refresh (to keep existing data visible)
                if (!isRefresh) {
                    _contests.value = NetworkResult.Loading
                }
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
            try {
                val result = contestRepository.getContestDetails(contestId)
                _contestDetails.value = result
            } catch (e: Exception) {
                _contestDetails.value = NetworkResult.Error(
                    message = "Failed to load contest details: ${e.message ?: "Unknown error"}"
                )
            } finally {
                _isLoading.value = false
            }
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


    fun refreshContests() {
        loadContests()
    }

    /**
     * Check voting eligibility for a photo
     * Simplified implementation: Uses daily voting logic for all contests
     */
    suspend fun checkVotingEligibility(photoId: Int, contestId: Int, voteFrequency: Int): VoteEligibilityResult {
        // For now, ignore the voteFrequency parameter and use daily voting logic
        // This ensures consistent 24-hour restrictions regardless of WordPress settings
        return voteTrackingRepository.canVote(contestId, photoId, VoteTrackingRepository.VOTE_FREQUENCY_DAILY)
    }

    /**
     * Update voting eligibility for all photos in current contest
     * Simplified implementation: Uses daily voting logic for all contests
     */
    fun updateVotingEligibilityForContest(contestId: Int, voteFrequency: Int, photos: List<Photo>) {
        viewModelScope.launch {
            val eligibilityMap = mutableMapOf<Int, VoteEligibilityResult>()
            photos.forEach { photo ->
                // Use daily voting logic regardless of actual vote frequency setting
                eligibilityMap[photo.id] = checkVotingEligibility(photo.id, contestId, VoteTrackingRepository.VOTE_FREQUENCY_DAILY)
            }
            _voteEligibility.value = eligibilityMap
        }
    }

    /**
     * Vote for a photo with eligibility checking and optimistic updates
     */
    fun votePhoto(photoId: Int, contestId: Int, voteFrequency: Int, email: String? = null) {
        viewModelScope.launch {
            try {
                // Check voting eligibility first
                val eligibility = checkVotingEligibility(photoId, contestId, voteFrequency)
                
                if (eligibility is VoteEligibilityResult.NotAllowed) {
                    _voteResult.value = NetworkResult.Error(eligibility.reason)
                    return@launch
                }

                // Add to voting set for loading state
                _votingPhotos.value = _votingPhotos.value + photoId
                
                // Record the vote locally first (optimistic update)
                voteTrackingRepository.recordVote(photoId, contestId)
                
                // Call the repository to vote
                val result = photoRepository.votePhoto(photoId, email)
                _voteResult.value = result
                
                // If successful, refresh contest photos to get updated vote counts
                if (result is NetworkResult.Success) {
                    // Update eligibility for this photo
                    val newEligibility = checkVotingEligibility(photoId, contestId, voteFrequency)
                    _voteEligibility.value = _voteEligibility.value.toMutableMap().apply {
                        this[photoId] = newEligibility
                    }
                    
                    // Refresh contest photos
                    loadContestPhotos(contestId)
                } else if (result is NetworkResult.Error) {
                    // If server rejects, remove the local vote record
                    // This is a rollback mechanism
                    voteTrackingRepository.clearContestVoteHistory(contestId)
                }
            } catch (e: Exception) {
                _voteResult.value = NetworkResult.Error("Vote failed: ${e.message ?: "Unknown error"}")
            } finally {
                // Remove from voting set
                _votingPhotos.value = _votingPhotos.value - photoId
            }
        }
    }

    /**
     * Clear vote result (call this after showing feedback to user)
     */
    fun clearVoteResult() {
        _voteResult.value = null
    }

    /**
     * Check if a photo is currently being voted on
     */
    fun isVoting(photoId: Int): Boolean {
        return _votingPhotos.value.contains(photoId)
    }

    /**
     * Get voting eligibility for a specific photo
     */
    fun getVoteEligibility(photoId: Int): VoteEligibilityResult {
        return _voteEligibility.value[photoId] ?: VoteEligibilityResult.Allowed
    }

    /**
     * Clear all vote history (for testing/debugging)
     */
    fun clearVoteHistory() {
        viewModelScope.launch {
            voteTrackingRepository.clearAllVoteHistory()
            _voteEligibility.value = emptyMap()
        }
    }
}
