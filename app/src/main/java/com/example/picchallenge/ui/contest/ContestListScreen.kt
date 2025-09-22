package com.example.picchallenge.ui.contest

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picchallenge.data.model.Contest
import com.example.picchallenge.ui.theme.*
import com.example.picchallenge.ui.viewmodel.ContestViewModel
import com.example.picchallenge.utils.HtmlContentParser
import com.example.picchallenge.utils.NetworkResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestListScreen(
    onContestClick: (Contest) -> Unit,
    onProfileClick: () -> Unit,
    contestViewModel: ContestViewModel = hiltViewModel()
) {
    val contests by contestViewModel.contests.collectAsState()
    val isLoading by contestViewModel.isLoading.collectAsState()
    
    // Load contests on initial composition with status=all
    LaunchedEffect(Unit) {
        contestViewModel.loadContests(status = "all")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Photo Contests",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contest List - No filters, always load all contests
            when (contests) {
                is NetworkResult.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is NetworkResult.Success -> {
                    val contestData = (contests as NetworkResult.Success).data
                    
                    if (contestData.data.isEmpty()) {
                        EmptyState(
                            message = "No contests found"
                        )
                    } else {
                        ContestList(
                            contests = contestData.data,
                            onContestClick = onContestClick,
                            onRefresh = {
                                contestViewModel.loadContests(status = "all")
                            }
                        )
                    }
                }
                is NetworkResult.Error -> {
                    ErrorState(
                        message = (contests as NetworkResult.Error).message,
                        onRetry = { 
                            contestViewModel.loadContests(status = "all")
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun ContestList(
    contests: List<Contest>,
    onContestClick: (Contest) -> Unit,
    onRefresh: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(contests) { contest ->
            ContestCard(
                contest = contest,
                onClick = { onContestClick(contest) }
            )
        }
    }
}

@Composable
private fun ContestCard(
    contest: Contest,
    onClick: () -> Unit
) {
    val imageUrls = remember { HtmlContentParser.parseImages(contest.description) }
    val firstImageUrl = contest.imageUrl ?: imageUrls.firstOrNull()
    val isEnded = contest.status.lowercase() == "ended"
    
    // Create grayscale color filter for ended contests
    val grayscaleColorFilter = if (isEnded) {
        ColorFilter.colorMatrix(ColorMatrix().apply {
            setToSaturation(0f) // 0f = fully grayscale
        })
    } else null
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp), // Increased height for more prominent image
        shape = RoundedCornerShape(20.dp), // More rounded corners
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEnded) 2.dp else 6.dp // Reduced elevation for ended contests
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image Section - More prominent with visual effects
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp) // Increased image height
            ) {
                if (firstImageUrl != null) {
                    // Load actual image using standard Image composable with grayscale filter for ended contests
                    val context = LocalContext.current
                    val imageRequest = remember(firstImageUrl) {
                        ImageRequest.Builder(context)
                            .data(firstImageUrl)
                            .crossfade(true)
                            .build()
                    }
                    
                    Image(
                        painter = rememberAsyncImagePainter(model = imageRequest),
                        contentDescription = "Contest image for ${contest.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = grayscaleColorFilter // Apply grayscale filter
                    )
                } else {
                    // Enhanced placeholder when no image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isEnded) Color(0xFFe5e7eb) else Color(0xFFf3f4f6)), // Different background for ended contests
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "No image",
                            tint = if (isEnded) Color(0xFF9ca3af) else Color(0xFF6b7280), // Different icon color for ended contests
                            modifier = Modifier.size(40.dp) // Larger icon
                        )
                    }
                }
                
                // Status overlay for better visibility
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (contest.status.lowercase()) {
                            "active" -> StatusGreen.copy(alpha = 0.9f)
                            "ended" -> StatusRed.copy(alpha = 0.9f)
                            "upcoming" -> StatusPurple.copy(alpha = 0.9f)
                            else -> Color.Gray.copy(alpha = 0.9f)
                        }
                    ) {
                        Text(
                            text = contest.status.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Content Section - Enhanced layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Increased padding
            ) {
                // Contest Name - Enhanced typography
                Text(
                    text = contest.name,
                    style = MaterialTheme.typography.titleMedium, // Larger text
                    fontWeight = FontWeight.Bold,
                    color = if (isEnded) TextGray.copy(alpha = 0.7f) else TextGray, // Faded text for ended contests
                    maxLines = 2, // Allow 2 lines for longer names
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp)) // More spacing
                
                // View Button - Enhanced styling
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth() // Full width button
                        .height(36.dp), // Slightly taller button
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnded) PrimaryBlue.copy(alpha = 0.6f) else PrimaryBlue, // Faded button for ended contests
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "View Contest",
                        style = MaterialTheme.typography.labelMedium, // Slightly larger text
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardWhite
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
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardWhite
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
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = StatusRed,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Error loading contests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextGray.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
