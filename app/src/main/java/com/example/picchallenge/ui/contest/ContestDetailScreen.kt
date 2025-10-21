package com.example.picchallenge.ui.contest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.picchallenge.data.model.VoteResponse
import com.example.picchallenge.utils.NetworkResult
import com.example.picchallenge.data.model.Contest
import com.example.picchallenge.data.model.Photo
import com.example.picchallenge.ui.theme.*
import com.example.picchallenge.ui.viewmodel.ContestViewModel
import com.example.picchallenge.ui.viewmodel.AuthViewModel
import com.example.picchallenge.ui.viewmodel.AuthState
import com.example.picchallenge.utils.HtmlContentParser
import com.example.picchallenge.data.repository.VoteEligibilityResult
import kotlinx.coroutines.delay

/**
 * Simple function to check if user is logged in
 * In a real app, this would check authentication state from repository
 */
private fun isUserLoggedIn(): Boolean {
    // For now, return false to hide the floating button
    // This can be updated to check actual authentication state
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestDetailScreen(
    contestId: Int,
    onNavigateBack: () -> Unit,
    onViewSubmissions: () -> Unit,
    onNavigateToLogin: () -> Unit,
    contestViewModel: ContestViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var contest by remember { mutableStateOf<Contest?>(null) }
    var contestPhotos by remember { mutableStateOf<List<Photo>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Use ViewModel's loading state instead of local state
    val isLoading by contestViewModel.isLoading.collectAsStateWithLifecycle()
    
    // Observe contest details and photos
    val contestDetails by contestViewModel.contestDetails.collectAsState()
    val contestPhotosResult by contestViewModel.contestPhotos.collectAsState()
    
    // Create a more comprehensive loading state that considers both contest details and photos
    val isDataLoading = remember(contestDetails, contestPhotosResult, isLoading) {
        isLoading || 
        contestDetails == null || 
        contestDetails is NetworkResult.Loading ||
        contestPhotosResult is NetworkResult.Loading
    }
    
    LaunchedEffect(contestId) {
        // Only load if we don't have the data already or if contestId changed
        val currentContestDetails = contestViewModel.contestDetails.value
        val shouldLoadDetails = currentContestDetails?.let { 
            it !is NetworkResult.Success || it.data.id != contestId 
        } ?: true
        
        val currentContestPhotos = contestViewModel.contestPhotos.value
        val shouldLoadPhotos = currentContestPhotos !is NetworkResult.Success || 
                              (currentContestPhotos is NetworkResult.Success && currentContestPhotos.data.data.isEmpty())
        
        if (shouldLoadDetails) {
            contestViewModel.loadContestDetails(contestId)
        }
        
        if (shouldLoadPhotos) {
            contestViewModel.loadContestPhotos(contestId)
        }
        
        // Get contest from existing data or load fresh
        when (val contestsResult = contestViewModel.contests.value) {
            is NetworkResult.Success -> {
                val foundContest = contestsResult.data.data.find { it.id == contestId }
                if (foundContest != null) {
                    contest = foundContest
                } else if (shouldLoadDetails) {
                    contestViewModel.loadContests()
                }
            }
            else -> {
                if (shouldLoadDetails) {
                    contestViewModel.loadContests()
                }
            }
        }
    }
    
    // Observe vote states
    val voteResult by contestViewModel.voteResult.collectAsStateWithLifecycle()
    val votingPhotos by contestViewModel.votingPhotos.collectAsStateWithLifecycle()
    val voteEligibility by contestViewModel.voteEligibility.collectAsStateWithLifecycle()
    
    // Observe authentication state
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    
    // Snackbar for vote feedback
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Periodic refresh of voting eligibility to update countdown timers
    LaunchedEffect(contestId, contestPhotos) {
        if (contestPhotos.isNotEmpty() && contest != null) {
            while (true) {
                delay(60000) // Refresh every minute
                contestPhotos.forEach { photo ->
                    contestViewModel.refreshVotingEligibility(
                        photo.id, 
                        contestId, 
                        contest?.voteFrequency ?: 1
                    )
                }
            }
        }
    }
    
    LaunchedEffect(contestDetails) {
        when (val result = contestDetails) {
            is NetworkResult.Success -> {
                if (result.data.id == contestId) {
                    contest = result.data
                }
            }
            else -> {}
        }
    }
    
    LaunchedEffect(contestPhotosResult) {
        when (val result = contestPhotosResult) {
            is NetworkResult.Success -> {
                contestPhotos = result.data.data
            }
            is NetworkResult.Error -> {
                error = result.message
            }
            else -> {}
        }
    }
    
    // Handle vote results with snackbar feedback
    LaunchedEffect(voteResult) {
        when (val result = voteResult) {
            is NetworkResult.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Vote recorded successfully! Total votes: ${result.data.newVoteCount}",
                    duration = SnackbarDuration.Short
                )
                contestViewModel.clearVoteResult()
            }
            is NetworkResult.Error -> {
                val message = result.message
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Long
                )
                
                // If authentication is required, navigate to login
                if (message.contains("login", ignoreCase = true) || 
                    message.contains("authentication", ignoreCase = true)) {
                    onNavigateToLogin()
                }
                
                contestViewModel.clearVoteResult()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .shadow(elevation = 4.dp)
                    .zIndex(1f),
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            contest?.name ?: "Contest Details",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = if (snackbarData.visuals.message.contains("failed")) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                }
            )
        }
    ) { paddingValues ->
        when {
            isDataLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                ErrorState(
                    message = error!!,
                    onRetry = { 
                        contestViewModel.loadContests()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            contest != null -> {
                ContestContent(
                    contest = contest!!,
                    contestPhotos = contestPhotos,
                    onViewSubmissions = onViewSubmissions,
                    onVotePhoto = { photo ->
                        contest?.let { currentContest ->
                            // Check authentication and voting eligibility using observed state
                            when (authState) {
                                is AuthState.Authenticated -> {
                                    // User is authenticated, proceed with voting
                                    contestViewModel.votePhoto(photo.id, currentContest.id, currentContest.voteFrequency)
                                }
                                is AuthState.Unauthenticated -> {
                                    // Navigate to login if not authenticated
                                    onNavigateToLogin()
                                }
                            }
                        }
                    },
                    contestViewModel = contestViewModel,
                    votingPhotos = votingPhotos,
                    voteEligibility = voteEligibility,
                    authState = authState,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ContestContent(
    contest: Contest,
    contestPhotos: List<Photo>,
    onViewSubmissions: () -> Unit,
    onVotePhoto: (Photo) -> Unit,
    contestViewModel: ContestViewModel,
    votingPhotos: Set<Int>,
    voteEligibility: Map<Int, VoteEligibilityResult>,
    authState: AuthState,
    modifier: Modifier = Modifier
) {
    val imageUrls = remember { HtmlContentParser.parseImages(contest.description) }
    val contestText = remember { HtmlContentParser.parseText(contest.description) }
    
    // Track which photos have expanded bios - hoisted to this level for stability
    var expandedPhotoIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Combined Image Carousel with Contest Details Overlay
        if (imageUrls.isNotEmpty()) {
            item {
                ContestImageWithOverlay(
                    imageUrls = imageUrls,
                    contest = contest,
                    contestText = contestText
                )
            }
        } else {
            // Fallback to header if no images
            item {
                ContestHeader(
                    contest = contest,
                    contestText = contestText
                )
            }
        }
        
        // Contestants List
        if (contestPhotos.isNotEmpty()) {
            item {
                ContestantsList(
                    photos = contestPhotos,
                    onVotePhoto = onVotePhoto,
                    contestStatus = contest.status,
                    contestId = contest.id,
                    contestViewModel = contestViewModel,
                    expandedPhotoIds = expandedPhotoIds,
                    onExpandedPhotoIdsChange = { expandedPhotoIds = it },
                    votingPhotos = votingPhotos,
                    voteEligibility = voteEligibility,
                    voteFrequency = contest.voteFrequency
                )
            }
        } else {
            item {
                EmptyContestantsMessage()
            }
        }
        
        // Action Buttons - removed View All Submissions
        item {
            ContestActions(
                contest = contest,
                onViewSubmissions = onViewSubmissions
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ContestHeader(
    contest: Contest,
    contestText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Status Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (contest.status.lowercase()) {
                    "active" -> StatusGreen
                    "ended" -> StatusRed
                    "upcoming" -> StatusPurple
                    else -> Color.Gray
                }
            ) {
                Text(
                    text = contest.status.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = contest.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = contestText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = contest.startDate.take(10),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column {
                    Text(
                        text = "End Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = contest.endDate.take(10),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ContestStats(
    contest: Contest
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Images/User",
                value = contest.imagePerUser.toString()
            )
            
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )
            
            StatItem(
                label = "Vote Frequency",
                value = when (contest.voteFrequency) {
                    1 -> "Daily"
                    7 -> "Weekly"
                    30 -> "Monthly"
                    else -> "${contest.voteFrequency} days"
                }
            )
            
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )
            
            StatItem(
                label = "Gallery Layout",
                value = when (contest.galleryLayout) {
                    1 -> "Grid"
                    2 -> "List"
                    else -> "Mixed"
                }
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ContestInfo(
    contest: Contest
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Contest Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "• Maximum ${contest.imagePerUser} photos per user\n" +
                       "• Vote frequency: ${contest.voteFrequency} day(s)\n" +
                       "• Contest mode: ${if (contest.contestMode == 1) "Standard" else "Premium"}\n" +
                       "• Status: ${contest.status.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        tint = StatusRed,
                        modifier = Modifier.size(64.dp)
                    )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Error loading contest",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

// New function to combine carousel with contest details overlay
@Composable
private fun ContestImageWithOverlay(
    imageUrls: List<String>,
    contest: Contest,
    contestText: String
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Auto-scrolling Image Carousel at the top - larger size
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AutoScrollingImageCarousel(
                imageUrls = imageUrls,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Contest Details Overlay (similar to ContestCard) - with rounded corners using Surface
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Status and dates in same row - more compact layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (contest.status.lowercase()) {
                            "active" -> StatusGreen
                            "ended" -> StatusRed
                            "upcoming" -> StatusPurple
                            else -> Color.Gray
                        }
                    ) {
                        Text(
                            text = contest.status.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Date Information
                    Text(
                        text = "${contest.startDate.take(10)} - ${contest.endDate.take(10)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = contestText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun AutoScrollingImageCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Simple auto-scroll effect
    LaunchedEffect(key1 = imageUrls.size) {
        if (imageUrls.size > 1) {
            while (true) {
                delay(3000) // Wait 3 seconds
                val maxScroll = scrollState.maxValue
                val currentScroll = scrollState.value
                
                if (currentScroll >= maxScroll) {
                    // Reset to beginning when reaching the end
                    scrollState.animateScrollTo(0)
                } else {
                    // Scroll to next position
                    val nextScroll = (currentScroll + 350).coerceAtMost(maxScroll)
                    scrollState.animateScrollTo(nextScroll)
                }
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display images twice for infinite scroll effect
        val displayUrls = imageUrls + imageUrls
        
        displayUrls.forEach { imageUrl ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
            ) {
                com.example.picchallenge.ui.components.EnhancedImage(
                    imageUrl = imageUrl,
                    contentDescription = "Contest image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun ContestantsList(
    photos: List<Photo>,
    onVotePhoto: (Photo) -> Unit,
    contestStatus: String,
    contestId: Int,
    contestViewModel: ContestViewModel,
    expandedPhotoIds: Set<Int>,
    onExpandedPhotoIdsChange: (Set<Int>) -> Unit,
    votingPhotos: Set<Int>,
    voteEligibility: Map<Int, VoteEligibilityResult>,
    voteFrequency: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contestants (${photos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            photos.forEach { photo ->
                val isBioExpanded = expandedPhotoIds.contains(photo.id)
                
                ContestantItem(
                    photo = photo,
                    onVoteClick = { onVotePhoto(photo) },
                    contestStatus = contestStatus,
                    isBioExpanded = isBioExpanded,
                    onBioToggle = { 
                        // Only toggle the list expansion, not the overlay
                        onExpandedPhotoIdsChange(
                            if (isBioExpanded) {
                                expandedPhotoIds - photo.id
                            } else {
                                expandedPhotoIds + photo.id
                            }
                        )
                    },
                    isVoting = votingPhotos.contains(photo.id),
                    voteEligibility = voteEligibility[photo.id]
                )
                if (photo != photos.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContestantItem(
    photo: Photo,
    onVoteClick: () -> Unit,
    contestStatus: String,
    isBioExpanded: Boolean,
    onBioToggle: () -> Unit,
    isVoting: Boolean = false,
    voteEligibility: VoteEligibilityResult?
) {
    // State for individual contestant overlay
    var showOverlay by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = androidx.compose.foundation.LocalIndication.current
                ) { 
                    println("Contestant clicked: ${photo.title} - Large URL: ${photo.large}")
                    showOverlay = true
                    // Don't call onBioToggle() here to prevent double bio display
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo thumbnail - removed individual overlay click handling
            Card(
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.picchallenge.ui.components.EnhancedImage(
                        imageUrl = photo.thumbnail,
                        contentDescription = photo.title ?: "Contestant photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // Contestant info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = photo.title ?: "Untitled Entry",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${photo.votes} votes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                // Add description/bio preview if available
                if (!photo.description.isNullOrEmpty()) {
                    Text(
                        text = photo.description.take(50) + if (photo.description.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Vote button with eligibility checking
            if (contestStatus.equals("active", ignoreCase = true)) {
                when (voteEligibility) {
                    is VoteEligibilityResult.NotAllowed -> {
                        // Show disabled button with restriction message
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "Voted",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = voteEligibility.reason,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    else -> {
                        // Normal vote button
                        Button(
                            onClick = { 
                                // Consume the click event to prevent it from bubbling up
                                onVoteClick()
                            },
                            enabled = !isVoting,
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (isVoting) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Text(
                                        "Voting...",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    "Vote",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ) {
                    Text(
                        "Ended",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Expanded bio section - appears below when clicked
        if (isBioExpanded && !photo.description.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Contestant Bio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryModern
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = photo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
    
    // Individual FullScreenBioViewer for this contestant
    if (showOverlay) {
        FullScreenBioViewer(
            photo = photo,
            onDismiss = { showOverlay = false }
        )
    }
}

@Composable
private fun EmptyContestantsMessage() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null,
                tint = PrimaryBlue.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "No submissions yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ContestActions(
    contest: Contest,
    onViewSubmissions: () -> Unit
) {
    // Upload photo button removed - now only available via floating action button for logged-in users
    // This function can be removed or kept for future use
}

@Composable
private fun FullScreenBioViewer(
    photo: Photo,
    onDismiss: () -> Unit
) {
    // Main container with drop shadow instead of semi-transparent overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background click area (outside the card)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )
        
        // Content area - centered card with bio information and drop shadow
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card container with drop shadow instead of semi-transparent background
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = photo.title ?: "Contestant Entry",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Bio/Description section
                    if (!photo.description.isNullOrEmpty()) {
                        Text(
                            text = "Contestant Bio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryModern
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = photo.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "No bio available for this contestant",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${photo.votes}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "Votes",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Spacer removed - no more dismiss hint text
        }
        
        // Close button in top right - positioned above everything with red color
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Red.copy(alpha = 0.8f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
