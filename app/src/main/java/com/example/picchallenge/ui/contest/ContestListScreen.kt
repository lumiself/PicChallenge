package com.example.picchallenge.ui.contest

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picchallenge.data.model.Contest
import com.example.picchallenge.data.model.ContestStatus
import com.example.picchallenge.ui.components.ContestCard
import com.example.picchallenge.ui.theme.*
import com.example.picchallenge.ui.viewmodel.ContestViewModel
import com.example.picchallenge.utils.NetworkResult
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

// Helper function to map String status to ContestStatus enum
fun mapStringToContestStatus(status: String?): ContestStatus {
    return when (status?.lowercase()) {
        "active" -> ContestStatus.ACTIVE
        "ended" -> ContestStatus.ENDED
        "upcoming" -> ContestStatus.UPCOMING
        else -> ContestStatus.ENDED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestListScreen(
    onContestClick: (Contest) -> Unit,
    contestViewModel: ContestViewModel = hiltViewModel()
) {
    val contestsResult by contestViewModel.contests.collectAsState()
    val isLoading by contestViewModel.isLoading.collectAsState()
    
    // State for debouncing refresh calls
    var lastRefreshTime by remember { mutableStateOf(0L) }
    val refreshDebounceMs = 1000L // 1 second debounce

    LaunchedEffect(Unit) {
        contestViewModel.loadContests(status = "all")
    }

    // Debounced refresh function
    val debouncedRefresh = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime > refreshDebounceMs) {
            lastRefreshTime = currentTime
            contestViewModel.loadContests(status = "all", isRefresh = true)
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
                            "Photo Contests",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val result = contestsResult) {
                is NetworkResult.Loading -> {
                    // Show loading only on initial load, not during refresh
                    if (!isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // During refresh, show the SwipeRefresh with existing data
                        SwipeRefreshContestGrid(
                            contests = emptyList(),
                            onContestClick = onContestClick,
                            isRefreshing = true,
                            onRefresh = debouncedRefresh
                        )
                    }
                }
                is NetworkResult.Success -> {
                    val contestData = result.data
                    if (contestData.data.isEmpty()) {
                        EmptyState(message = "No contests found")
                    } else {
                        SwipeRefreshContestGrid(
                            contests = contestData.data,
                            onContestClick = onContestClick,
                            isRefreshing = isLoading,
                            onRefresh = debouncedRefresh
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
private fun SwipeRefreshContestGrid(
    contests: List<Contest>,
    onContestClick: (Contest) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
    
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(contests, key = { it.id }) { contest ->
                ContestCard(
                    contest = contest,
                    onClick = { onContestClick(contest) }
                )
            }
        }
    }
}

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = StatusRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Error loading contests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text(message ?: "Unknown error", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}
