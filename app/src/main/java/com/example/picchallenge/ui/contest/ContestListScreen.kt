package com.example.picchallenge.ui.contest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh // Keep for error/empty states if needed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picchallenge.data.model.Contest // Your existing Contest model
import com.example.picchallenge.data.model.ContestStatus // Ensure this enum is created
import com.example.picchallenge.ui.components.ContestCard // Ensure this composable is created from the guide
import com.example.picchallenge.ui.theme.*
import com.example.picchallenge.ui.viewmodel.ContestViewModel
import com.example.picchallenge.utils.HtmlContentParser
import com.example.picchallenge.utils.NetworkResult

// Helper function to map String status to ContestStatus enum
fun mapStringToContestStatus(status: String?): ContestStatus {
    return when (status?.lowercase()) {
        "active" -> ContestStatus.ACTIVE
        "ended" -> ContestStatus.ENDED
        "upcoming" -> ContestStatus.UPCOMING
        else -> ContestStatus.ENDED // Default or handle as an error/unknown state
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestListScreen(
    onContestClick: (com.example.picchallenge.data.model.Contest) -> Unit, // Existing click takes the old model
    onProfileClick: () -> Unit,
    contestViewModel: ContestViewModel = hiltViewModel()
) {
    val contestsResult by contestViewModel.contests.collectAsState()
    // val isLoading by contestViewModel.isLoading.collectAsState() // isLoading seems to be derived from contestsResult

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
            when (val result = contestsResult) {
                is NetworkResult.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is NetworkResult.Success -> {
                    val contestData = result.data
                    if (contestData.data.isEmpty()) {
                        EmptyState(
                            message = "No contests found"
                        )
                    } else {
                        ActualContestGrid(
                            contests = contestData.data, // Pass the original list
                            onContestClick = onContestClick // Original click handler
                        )
                    }
                }
                is NetworkResult.Error -> {
                    ErrorState(
                        message = result.message,
                        onRetry = { contestViewModel.loadContests(status = "all") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActualContestGrid(
    contests: List<com.example.picchallenge.data.model.Contest>, // Takes the original Contest model
    onContestClick: (com.example.picchallenge.data.model.Contest) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contests, key = { it.id }) { contest ->
            ContestCard(
                contest = contest,
                onClick = { onContestClick(contest) }
            )
        }
    }
}


// Empty and Error states can remain as they are or be styled further
@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh, // Consider a more relevant icon
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = TextGray)
            }
        }
    }
}

@Composable
private fun ErrorState(message: String?, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh, // Consider a more relevant error icon
                    contentDescription = null,
                    tint = StatusRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Error loading contests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(message ?: "Unknown error", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = TextGray.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}
