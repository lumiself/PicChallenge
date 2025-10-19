# 🗳️ Photo Contest Voting System Integration Guide
## Complete Voting Implementation for Android Apps
**Production URL**: `https://lumiself.co.zw/modeling`

---

## 🎯 Overview

This guide provides a comprehensive implementation of the photo contest voting system specifically focused on voting functionality. It covers everything from checking voting eligibility to submitting votes and tracking voting history.

---

## 🚀 Quick Voting Flow

```kotlin
// Complete voting flow in 3 steps:
// 1. Check if user can vote
// 2. Submit vote for photo
// 3. Track voting history

val canVote = checkVotingEligibility(contestId)
if (canVote.can_vote) {
    val voteResult = submitVote(photoId, contestId)
    updateVoteHistory(photoId, contestId)
}
```

---

## 📋 Voting API Endpoints

### 🔒 Authenticated Voting Endpoints

| Endpoint | Method | Headers | Description |
|----------|--------|---------|-------------|
| `/wp-json/jwt-um/v1/user/can-vote` | GET | `Authorization: Bearer {token}` | Check if user can vote in contest |
| `/wp-json/photo-contest/v1/photos/{id}/vote` | POST | `Authorization: Bearer {token}` | Vote for specific photo |
| `/wp-json/jwt-um/v1/user/voting-history` | GET | `Authorization: Bearer {token}` | Get user's voting history |

### 🔓 Public Voting Information

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/wp-json/photo-contest/v1/photos/{id}` | GET | Get photo details including vote count |
| `/wp-json/photo-contest/v1/contests/{id}/photos` | GET | Get contest photos with vote counts |

---

## 🏗️ Complete Voting Implementation

### 1. Voting Data Models

```kotlin
// Voting Request/Response Models
data class VoteRequest(
    val email: String  // User email for voting
)

data class VoteResponse(
    val success: Boolean,
    val message: String,
    val new_vote_count: Int  // Updated vote count after voting
)

data class VotingEligibility(
    val can_vote: Boolean,
    val reason: String?,  // Reason if cannot vote
    val message: String   // Human readable message
)

data class VoteHistory(
    val id: Int,
    val photo_id: Int,
    val photo_title: String,
    val contest_id: Int,
    val contest_name: String,
    val vote_date: String
)

// Photo with Voting Info
data class PhotoWithVotes(
    val id: Int,
    val title: String,
    val description: String,
    val url: String,
    val thumbnail: String,
    val votes: Int,
    val views: Int,
    val author: String,
    val contest_id: Int,
    val has_voted: Boolean = false  // Local tracking
)
```

### 2. Voting Repository

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VotingRepository @Inject constructor(
    private val photoContestApi: PhotoContestApi,
    private val jwtUserApi: JWTUserApi,
    private val tokenManager: TokenManager
) {

    /**
     * Check if user can vote in a specific contest
     * Returns VotingEligibility with detailed information
     */
    suspend fun checkVotingEligibility(contestId: Int): Result<VotingEligibility> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken() 
                    ?: return@withContext Result.success(
                        VotingEligibility(
                            can_vote = false,
                            reason = "not_logged_in",
                            message = "Please login to vote"
                        )
                    )

                val response = jwtUserApi.canUserVote("Bearer $token", contestId)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Submit a vote for a specific photo in a contest
     * Automatically checks eligibility before voting
     */
    suspend fun submitVote(photoId: Int, contestId: Int): Result<VoteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get user token and data
                val token = tokenManager.getToken() 
                    ?: throw Exception("User not logged in")
                
                val user = tokenManager.getUserData() 
                    ?: throw Exception("User data not found")

                // Check eligibility first
                val eligibility = checkVotingEligibility(contestId).getOrThrow()
                if (!eligibility.can_vote) {
                    throw Exception(eligibility.message)
                }

                // Submit vote
                val response = photoContestApi.voteForPhoto(
                    "Bearer $token",
                    photoId,
                    VoteRequest(user.email)
                )

                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get user's voting history for specific contest or all contests
     */
    suspend fun getVotingHistory(contestId: Int? = null): Result<List<VoteHistory>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken() 
                    ?: throw Exception("User not logged in")

                val response = jwtUserApi.getVotingHistory(
                    "Bearer $token", 
                    contestId
                )
                
                Result.success(response.data)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get photos with voting status for current user
     */
    suspend fun getPhotosWithVotingStatus(contestId: Int): Result<List<PhotoWithVotes>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                val photos = photoContestApi.getContestPhotos(contestId).data
                
                // If user is logged in, check which photos they've voted for
                val photosWithStatus = if (token != null) {
                    val votingHistory = getVotingHistory(contestId).getOrNull() ?: emptyList()
                    val votedPhotoIds = votingHistory.map { it.photo_id }.toSet()
                    
                    photos.map { photo ->
                        PhotoWithVotes(
                            id = photo.id,
                            title = photo.title,
                            description = photo.description,
                            url = photo.url,
                            thumbnail = photo.thumbnail,
                            votes = photo.votes,
                            views = photo.views,
                            author = photo.author,
                            contest_id = photo.contest_id,
                            has_voted = votedPhotoIds.contains(photo.id)
                        )
                    }
                } else {
                    // User not logged in, mark all as not voted
                    photos.map { photo ->
                        PhotoWithVotes(
                            id = photo.id,
                            title = photo.title,
                            description = photo.description,
                            url = photo.url,
                            thumbnail = photo.thumbnail,
                            votes = photo.votes,
                            views = photo.views,
                            author = photo.author,
                            contest_id = photo.contest_id,
                            has_voted = false
                        )
                    }
                }
                
                Result.success(photosWithStatus)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get vote statistics for a contest
     */
    suspend fun getContestVoteStats(contestId: Int): Result<VoteStats> {
        return withContext(Dispatchers.IO) {
            try {
                val photos = photoContestApi.getContestPhotos(contestId).data
                
                val totalVotes = photos.sumOf { it.votes }
                val averageVotes = if (photos.isNotEmpty()) totalVotes / photos.size else 0
                val mostVotedPhoto = photos.maxByOrNull { it.votes }
                val leastVotedPhoto = photos.minByOrNull { it.votes }
                
                Result.success(
                    VoteStats(
                        total_votes = totalVotes,
                        total_photos = photos.size,
                        average_votes = averageVotes,
                        most_voted_photo = mostVotedPhoto,
                        least_voted_photo = leastVotedPhoto,
                        photos_count = photos.size
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

data class VoteStats(
    val total_votes: Int,
    val total_photos: Int,
    val average_votes: Int,
    val most_voted_photo: Photo?,
    val least_voted_photo: Photo?,
    val photos_count: Int
)
```

### 3. Voting ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VotingViewModel @Inject constructor(
    private val votingRepository: VotingRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _votingState = MutableStateFlow<VotingState>(VotingState.Idle)
    val votingState: StateFlow<VotingState> = _votingState.asStateFlow()

    private val _eligibilityState = MutableStateFlow<EligibilityState>(EligibilityState.Checking)
    val eligibilityState: StateFlow<EligibilityState> = _eligibilityState.asStateFlow()

    private val _voteHistory = MutableStateFlow<List<VoteHistory>>(emptyList())
    val voteHistory: StateFlow<List<VoteHistory>> = _voteHistory.asStateFlow()

    private val _voteStats = MutableStateFlow<VoteStats?>(null)
    val voteStats: StateFlow<VoteStats?> = _voteStats.asStateFlow()

    private val _currentContestId = MutableStateFlow<Int?>(null)
    val currentContestId: StateFlow<Int?> = _currentContestId.asStateFlow()

    /**
     * Check voting eligibility for a specific contest
     */
    fun checkVotingEligibility(contestId: Int) {
        viewModelScope.launch {
            _eligibilityState.value = EligibilityState.Checking
            _currentContestId.value = contestId
            
            when (val result = votingRepository.checkVotingEligibility(contestId)) {
                is Result.Success -> {
                    val eligibility = result.getOrNull()!!
                    _eligibilityState.value = when {
                        eligibility.can_vote -> EligibilityState.Eligible(eligibility)
                        else -> EligibilityState.NotEligible(eligibility)
                    }
                }
                is Result.Failure -> {
                    _eligibilityState.value = EligibilityState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to check eligibility"
                    )
                }
            }
        }
    }

    /**
     * Submit a vote for a specific photo
     */
    fun submitVote(photoId: Int, contestId: Int) {
        viewModelScope.launch {
            _votingState.value = VotingState.Submitting
            
            when (val result = votingRepository.submitVote(photoId, contestId)) {
                is Result.Success -> {
                    val response = result.getOrNull()!!
                    _votingState.value = VotingState.Success(
                        message = response.message,
                        newVoteCount = response.new_vote_count
                    )
                    
                    // Reload eligibility and history after successful vote
                    checkVotingEligibility(contestId)
                    loadVotingHistory(contestId)
                }
                is Result.Failure -> {
                    _votingState.value = VotingState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to submit vote"
                    )
                }
            }
        }
    }

    /**
     * Load voting history for current contest or all contests
     */
    fun loadVotingHistory(contestId: Int? = null) {
        viewModelScope.launch {
            when (val result = votingRepository.getVotingHistory(contestId)) {
                is Result.Success -> {
                    _voteHistory.value = result.getOrNull() ?: emptyList()
                }
                is Result.Failure -> {
                    // Handle error silently or show message
                    _voteHistory.value = emptyList()
                }
            }
        }
    }

    /**
     * Load vote statistics for a contest
     */
    fun loadVoteStats(contestId: Int) {
        viewModelScope.launch {
            when (val result = votingRepository.getContestVoteStats(contestId)) {
                is Result.Success -> {
                    _voteStats.value = result.getOrNull()
                }
                is Result.Failure -> {
                    _voteStats.value = null
                }
            }
        }
    }

    /**
     * Reset voting state to idle
     */
    fun resetVotingState() {
        _votingState.value = VotingState.Idle
    }

    /**
     * Clear all voting data (for logout)
     */
    fun clearVotingData() {
        _eligibilityState.value = EligibilityState.Checking
        _voteHistory.value = emptyList()
        _voteStats.value = null
        _currentContestId.value = null
        _votingState.value = VotingState.Idle
    }
}

// Voting States
sealed class VotingState {
    object Idle : VotingState()
    object Submitting : VotingState()
    data class Success(val message: String, val newVoteCount: Int) : VotingState()
    data class Error(val message: String) : VotingState()
}

sealed class EligibilityState {
    object Checking : EligibilityState()
    data class Eligible(val eligibility: VotingEligibility) : EligibilityState()
    data class NotEligible(val eligibility: VotingEligibility) : EligibilityState()
    data class Error(val message: String) : EligibilityState()
}
```

### 4. Voting UI Components

```kotlin
// Custom Voting Button
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class VotingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var contestId: Int = -1
    private var photoId: Int = -1
    private var votingViewModel: VotingViewModel? = null

    init {
        text = "Vote"
        setOnClickListener { handleVoteClick() }
    }

    fun setupVoteButton(
        contestId: Int,
        photoId: Int,
        viewModel: VotingViewModel
    ) {
        this.contestId = contestId
        this.photoId = photoId
        this.votingViewModel = viewModel
        
        // Check initial eligibility
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            viewModel.checkVotingEligibility(contestId)
        }
    }

    private fun handleVoteClick() {
        if (contestId == -1 || photoId == -1) return
        
        votingViewModel?.let { viewModel ->
            findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                viewModel.submitVote(photoId, contestId)
            }
        }
    }

    fun updateButtonState(eligibility: VotingEligibility) {
        isEnabled = eligibility.can_vote
        text = if (eligibility.can_vote) "Vote" else eligibility.message
        alpha = if (eligibility.can_vote) 1.0f else 0.6f
    }
}

// Voting Eligibility Display
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class VotingEligibilityView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val titleText: TextView
    private val messageText: TextView
    private val statusIcon: View

    init {
        // Inflate custom layout
        inflate(context, R.layout.view_voting_eligibility, this)
        
        titleText = findViewById(R.id.eligibilityTitle)
        messageText = findViewById(R.id.eligibilityMessage)
        statusIcon = findViewById(R.id.statusIcon)
        
        radius = 12f
        cardElevation = 4f
    }

    fun displayEligibility(eligibility: VotingEligibility) {
        when {
            eligibility.can_vote -> {
                titleText.text = "You can vote!"
                messageText.text = "Click the vote button to cast your vote"
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.green_light))
                statusIcon.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
            }
            else -> {
                titleText.text = "Cannot vote"
                messageText.text = eligibility.message
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.red_light))
                statusIcon.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
            }
        }
    }
}
```

### 5. Complete Voting Activity

```kotlin
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VotingActivity : AppCompatActivity() {

    private val votingViewModel: VotingViewModel by viewModels()
    private lateinit var photosAdapter: PhotosWithVotingAdapter
    
    private var contestId: Int = -1
    private var contestName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voting)
        
        // Get contest data from intent
        contestId = intent.getIntExtra("contest_id", -1)
        contestName = intent.getStringExtra("contest_name") ?: "Contest"
        
        if (contestId == -1) {
            Toast.makeText(this, "Invalid contest", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupViews()
        observeViewModel()
        loadVotingData()
    }
    
    private fun setupViews() {
        title = "$contestName - Voting"
        
        // Setup RecyclerView
        photosAdapter = PhotosWithVotingAdapter(
            onVoteClick = { photo ->
                handleVoteClick(photo)
            }
        )
        
        photosRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@VotingActivity)
            adapter = photosAdapter
        }
        
        // Setup vote stats
        voteStatsButton.setOnClickListener {
            showVoteStats()
        }
        
        // Setup refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadVotingData()
        }
    }
    
    private fun observeViewModel() {
        // Observe eligibility
        votingViewModel.eligibilityState.observe(this) { state ->
            when (state) {
                is EligibilityState.Eligible -> {
                    eligibilityView.displayEligibility(state.eligibility)
                    photosAdapter.setVotingEnabled(true)
                }
                is EligibilityState.NotEligible -> {
                    eligibilityView.displayEligibility(state.eligibility)
                    photosAdapter.setVotingEnabled(false)
                }
                is EligibilityState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    photosAdapter.setVotingEnabled(false)
                }
                else -> { /* Loading state */ }
            }
        }
        
        // Observe voting state
        votingViewModel.votingState.observe(this) { state ->
            when (state) {
                is VotingState.Submitting -> showLoading(true)
                is VotingState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    // Update vote count in adapter
                    photosAdapter.updateVoteCount(state.newVoteCount)
                }
                is VotingState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> showLoading(false)
            }
        }
        
        // Observe photos
        votingViewModel.photosWithVotingStatus.observe(this) { photos ->
            photosAdapter.submitList(photos)
            swipeRefreshLayout.isRefreshing = false
        }
        
        // Observe vote stats
        votingViewModel.voteStats.observe(this) { stats ->
            stats?.let {
                updateVoteStatsDisplay(it)
            }
        }
    }
    
    private fun loadVotingData() {
        swipeRefreshLayout.isRefreshing = true
        
        // Load contest photos with voting status
        lifecycleScope.launch {
            votingViewModel.loadPhotosWithVotingStatus(contestId)
            votingViewModel.checkVotingEligibility(contestId)
            votingViewModel.loadVoteStats(contestId)
            votingViewModel.loadVotingHistory(contestId)
        }
    }
    
    private fun handleVoteClick(photo: PhotoWithVotes) {
        if (photo.has_voted) {
            Toast.makeText(this, "You have already voted for this photo", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show confirmation dialog
        showVoteConfirmationDialog(photo)
    }
    
    private fun showVoteConfirmationDialog(photo: PhotoWithVotes) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Vote")
            .setMessage("Are you sure you want to vote for \"${photo.title}\"?")
            .setPositiveButton("Vote") { _, _ ->
                votingViewModel.submitVote(photo.id, contestId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showVoteStats() {
        val stats = votingViewModel.voteStats.value
        stats?.let {
            val message = """
                Contest Statistics:
                • Total Votes: ${it.total_votes}
                • Total Photos: ${it.total_photos}
                • Average Votes: ${it.average_votes}
                • Most Voted: ${it.most_voted_photo?.title ?: "N/A"}
            """.trimIndent()
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Vote Statistics")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
    
    private fun updateVoteStatsDisplay(stats: VoteStats) {
        totalVotesText.text = "Total Votes: ${stats.total_votes}"
        totalPhotosText.text = "Photos: ${stats.total_photos}"
        averageVotesText.text = "Avg: ${stats.average_votes}"
    }
    
    private fun showLoading(show: Boolean) {
        swipeRefreshLayout.isRefreshing = show
        // Disable vote buttons during loading
        photosAdapter.setLoadingState(show)
    }
}
```

---

## 🧪 Testing Voting Functionality

### 1. Test Voting Eligibility
```kotlin
@Test
fun testVotingEligibility() = runTest {
    val result = votingRepository.checkVotingEligibility(1)
    assertTrue(result.isSuccess)
    
    val eligibility = result.getOrNull()
    assertNotNull(eligibility)
    println("Can vote: ${eligibility?.can_vote}")
    println("Reason: ${eligibility?.reason}")
    println("Message: ${eligibility?.message}")
}
```

### 2. Test Vote Submission
```kotlin
@Test
fun testVoteSubmission() = runTest {
    // First login to get token
    val loginResult = authRepository.loginUser("testuser", "password123")
    assertTrue(loginResult.isSuccess)
    
    // Then submit vote
    val voteResult = votingRepository.submitVote(123, 1)
    assertTrue(voteResult.isSuccess)
    
    val response = voteResult.getOrNull()
    assertNotNull(response)
    println("Vote submitted: ${response?.message}")
    println("New vote count: ${response?.new_vote_count}")
}
```

### 3. Test Voting History
```kotlin
@Test
fun testVotingHistory() = runTest {
    val result = votingRepository.getVotingHistory(1)
    assertTrue(result.isSuccess)
    
    val history = result.getOrNull()
    println("Vote history count: ${history?.size}")
    history?.forEach { vote ->
        println("Voted for: ${vote.photo_title} in ${vote.contest_name}")
    }
}
```

### 4. Test Complete Voting Flow
```kotlin
@Test
fun testCompleteVotingFlow() = runTest {
    // 1. Check eligibility
    val eligibility = votingRepository.checkVotingEligibility(1).getOrThrow()
    assertTrue(eligibility.can_vote)
    
    // 2. Get photos with voting status
    val photos = votingRepository.getPhotosWithVotingStatus(1).getOrThrow()
    val photoToVote = photos.first { !it.has_voted }
    
    // 3. Submit vote
    val voteResult = votingRepository.submitVote(photoToVote.id, 1).getOrThrow()
    assertTrue(voteResult.success)
    
    // 4. Verify vote was recorded
    val updatedHistory = votingRepository.getVotingHistory(1).getOrThrow()
    assertTrue(updatedHistory.any { it.photo_id == photoToVote.id })
}
```

---

## 📊 Vote Analytics & Statistics

### Vote Statistics Implementation
```kotlin
data class VoteAnalytics(
    val totalVotes: Int,
    val uniqueVoters: Int,
    val averageVotesPerPhoto: Double,
    val mostPopularPhoto: Photo?,
    val votingTrend: List<VoteTrend>,
    val hourlyDistribution: Map<Int, Int> // Hour -> Vote count
)

data class VoteTrend(
    val date: String,
    val voteCount: Int
)

class VoteAnalyticsHelper @Inject constructor(
    private val votingRepository: VotingRepository
) {

    suspend fun generateVoteAnalytics(contestId: Int): Result<VoteAnalytics> {
        return withContext(Dispatchers.IO) {
            try {
                // Get all photos in contest
                val photos = votingRepository.getPhotosWithVotingStatus(contestId).getOrThrow()
                
                // Get voting history
                val voteHistory = votingRepository.getVotingHistory(contestId).getOrThrow()
                
                // Calculate statistics
                val totalVotes = photos.sumOf { it.votes }
                val uniqueVoters = voteHistory.map { it.id }.distinct().count()
                val averageVotes = if (photos.isNotEmpty()) totalVotes.toDouble() / photos.size else 0.0
                val mostPopular = photos.maxByOrNull { it.votes }
                
                // Generate voting trend (daily)
                val votingTrend = voteHistory
                    .groupBy { it.vote_date.substring(0, 10) } // Group by date
                    .map { (date, votes) -> VoteTrend(date, votes.size) }
                    .sortedBy { it.date }
                
                Result.success(
                    VoteAnalytics(
                        totalVotes = totalVotes,
                        uniqueVoters = uniqueVoters,
                        averageVotesPerPhoto = averageVotes,
                        mostPopularPhoto = mostPopular,
                        votingTrend = votingTrend,
                        hourlyDistribution = emptyMap() // Would need additional data
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

---

## 🛡️ Voting Security & Validation

### Vote Validation Rules
```kotlin
object VoteValidationRules {

    fun validateVoteSubmission(
        photoId: Int,
        contestId: Int,
        userId: Int,
        existingVotes: List<VoteHistory>
    ): VoteValidationResult {
        return when {
            // Check if user already voted for this photo
            existingVotes.any { it.photo_id == photoId } -> {
                VoteValidationResult.Invalid("You have already voted for this photo")
            }
            
            // Check if contest is still active
            !isContestActive(contestId) -> {
                VoteValidationResult.Invalid("Contest is no longer active")
            }
            
            // Check if voting period is open
            !isVotingPeriodOpen(contestId) -> {
                VoteValidationResult.Invalid("Voting period is not open")
            }
            
            // Check vote frequency limits
            hasExceededVoteLimit(userId, contestId, existingVotes) -> {
                VoteValidationResult.Invalid("You have reached the maximum number of votes for this contest")
            }
            
            else -> VoteValidationResult.Valid
        }
    }

    private fun isContestActive(contestId: Int): Boolean {
        // Implementation would check contest dates
        return true // Simplified for example
    }

    private fun isVotingPeriodOpen(contestId: Int): Boolean {
        // Implementation would check voting period dates
        return true // Simplified for example
    }

    private fun hasExceededVoteLimit(userId: Int, contestId: Int, existingVotes: List<VoteHistory>): Boolean {
        // Check against contest vote_frequency setting
        val maxVotes = getContestMaxVotes(contestId) // Would come from contest data
        val userVotesInContest = existingVotes.count { it.contest_id == contestId }
        return userVotesInContest >= maxVotes
    }

    private fun getContestMaxVotes(contestId: Int): Int {
        // This would normally come from contest configuration
        return 1 // Default to 1 vote per contest
    }
}

sealed class VoteValidationResult {
    object Valid : VoteValidationResult()
    data class Invalid(val reason: String) : VoteValidationResult()
}
```

### Anti-Fraud Measures
```kotlin
class VoteSecurityManager @Inject constructor(
    private val deviceInfoProvider: DeviceInfoProvider,
    private val ipAddressProvider: IPAddressProvider
) {

    suspend fun validateVoteSecurity(
        userId: Int,
        photoId: Int,
        contestId: Int
    ): SecurityValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                val deviceId = deviceInfoProvider.getDeviceId()
                val ipAddress = ipAddressProvider.getIPAddress()
                val timestamp = System.currentTimeMillis()

                // Check for suspicious patterns
                val checks = listOf(
                    checkDeviceDuplication(deviceId, contestId),
                    checkIPDuplication(ipAddress, contestId),
                    checkRateLimiting(userId, timestamp),
                    checkBotBehavior(userId, timestamp)
                )

                val failedChecks = checks.filterIsInstance<SecurityCheck.Failed>()
                
                if (failedChecks.isEmpty()) {
                    SecurityValidationResult.Passed
                } else {
                    SecurityValidationResult.Failed(failedChecks.map { it.reason })
                }
            } catch (e: Exception) {
                SecurityValidationResult.Error(e.message ?: "Security validation failed")
            }
        }
    }

    private fun checkDeviceDuplication(deviceId: String, contestId: Int): SecurityCheck {
        // Implementation would check if this device already voted
        return SecurityCheck.Passed
    }

    private fun checkIPDuplication(ipAddress: String, contestId: Int): SecurityCheck {
        // Implementation would check if this IP already voted
        return SecurityCheck.Passed
    }

    private fun checkRateLimiting(userId: Int, timestamp: Long): SecurityCheck {
        // Implementation would check voting rate limits
        return SecurityCheck.Passed
    }

    private fun checkBotBehavior(userId: Int, timestamp: Long): SecurityCheck {
        // Implementation would detect bot-like behavior
        return SecurityCheck.Passed
    }
}

sealed class SecurityCheck {
    object Passed : SecurityCheck()
    data class Failed(val reason: String) : SecurityCheck()
}

sealed class SecurityValidationResult {
    object Passed : SecurityValidationResult()
    data class Failed(val reasons: List<String>) : SecurityValidationResult()
    data class Error(val message: String) : SecurityValidationResult()
}
```

---

## 🎨 Voting UI/UX Best Practices

### Voting Button States
```xml
<!-- res/drawable/vote_button_selector.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_enabled="false">
        <shape android:shape="rectangle">
            <solid android:color="@color/grey_300"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/primary_dark"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/primary"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
</selector>
```

### Vote Confirmation Dialog
```kotlin
private fun showVoteConfirmation(photo: PhotoWithVotes) {
    MaterialAlertDialogBuilder(context)
        .setTitle("Confirm Your Vote")
        .setMessage("""
            You are about to vote for:
            
            "${photo.title}"
            by ${photo.author}
            
            This action cannot be undone.
        """.trimIndent())
        .setPositiveButton("Vote") { _, _ ->
            submitVote(photo.id, photo.contest_id)
        }
        .setNegativeButton("Cancel", null)
        .setNeutralButton("View Details") { _, _ ->
            showPhotoDetails(photo)
        }
        .show()
}
```

### Vote Success Animation
```kotlin
private fun showVoteSuccess(voteCount: Int) {
    // Success animation
    val successView = layoutInflater.inflate(R.layout.vote_success_animation, null)
    val voteCountText = successView.findViewById<TextView>(R.id.voteCountText)
    voteCountText.text = "Vote recorded!\nTotal votes: $voteCount"
    
    val dialog = MaterialAlertDialogBuilder(context)
        .setView(successView)
        .setCancelable(false)
        .create()
    
    dialog.show()
    
    // Auto-dismiss after animation
    Handler(Looper.getMainLooper()).postDelayed({
        dialog.dismiss()
    }, 2000)
}
```

---

## 📱 Mobile-Specific Voting Features

### Swipe to Vote
```kotlin
class SwipeToVoteCallback(
    private val onVote: (PhotoWithVotes) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val photo = (viewHolder as PhotoViewHolder).photo
        if (!photo.has_voted) {
            onVote(photo)
        } else {
            // Reset the swipe if already voted
            viewHolder.bindingAdapter?.notifyItemChanged(viewHolder.adapterPosition)
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val paint = Paint().apply {
                color = if (dX > 0) Color.GREEN else Color.BLUE
                alpha = (255 * abs(dX) / itemView.width).toInt()
            }
            
            canvas.drawRect(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat(),
                paint
            )
            
            // Draw vote icon
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_vote)
            icon?.setBounds(
                itemView.left + 50,
                itemView.top + (itemView.height - 100) / 2,
                itemView.left + 150,
                itemView.top + (itemView.height + 100) / 2
            )
            icon?.draw(canvas)
        }
        
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
```

### Haptic Feedback
```kotlin
private fun provideHapticFeedback() {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(50)
    }
}
```

---

## 🧪 Complete Voting Testing Suite

### 1. Test Voting Eligibility
```kotlin
@Test
fun testVotingEligibility() = runTest {
    val result = votingRepository.checkVotingEligibility(1)
    assertTrue(result.isSuccess)
    
    val eligibility = result.getOrNull()
    assertNotNull(eligibility)
    
    println("✓ Can vote: ${eligibility?.can_vote}")
    println("✓ Reason: ${eligibility?.reason}")
    println("✓ Message: ${eligibility?.message}")
}
```

### 2. Test Vote Submission Flow
```kotlin
@Test
fun testCompleteVoteSubmission() = runTest {
    println("🗳️ Testing complete vote submission...")
    
    // 1. Check eligibility
    println("1. Checking voting eligibility...")
    val eligibility = votingRepository.checkVotingEligibility(1).getOrThrow()
    assertTrue("User should be eligible to vote", eligibility.can_vote)
    println("   ✓ User is eligible to vote")
    
    // 2. Get photos with voting status
    println("2. Getting photos with voting status...")
    val photos = votingRepository.getPhotosWithVotingStatus(1).getOrThrow()
    assertTrue("Should have photos to vote for", photos.isNotEmpty())
    println("   ✓ Found ${photos.size} photos")
    
    // 3. Select a photo that hasn't been voted for
    val photoToVote = photos.first { !it.has_voted }
    println("3. Selected photo: ${photoToVote
