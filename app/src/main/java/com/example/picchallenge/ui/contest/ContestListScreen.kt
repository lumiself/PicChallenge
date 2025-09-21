package com.example.picchallenge.ui.contest

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    val firstImageUrl = imageUrls.firstOrNull()
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image Section - Full width on top with actual image loading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (firstImageUrl != null) {
                    // Load actual image using EnhancedImage component
                    com.example.picchallenge.ui.components.EnhancedImage(
                        imageUrl = firstImageUrl,
                        contentDescription = "Contest image for ${contest.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder when no image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "No image",
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            // Content Section - Below image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Contest Name
                Text(
                    text = contest.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status and View Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Text (like in prototype)
                    Text(
                        text = contest.status.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (contest.status.lowercase()) {
                            "active" -> StatusGreen
                            "ended" -> StatusRed
                            "upcoming" -> StatusPurple
                            else -> Color.Gray
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // View Button (prominent like in prototype)
                    Button(
                        onClick = onClick,
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "View",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
