package com.example.picchallenge.ui.submissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picchallenge.ui.theme.*
import com.example.picchallenge.ui.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySubmissionsScreen(
    onNavigateBack: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    // Simple state - assume not logged in for demo
    var isLoggedIn by remember { mutableStateOf(false) }
    var showSampleData by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Simulate checking login status
        kotlinx.coroutines.delay(500)
        isLoggedIn = true
        showSampleData = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "My Submissions",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when {
            !isLoggedIn -> {
                NotLoggedInState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            showSampleData -> {
                SampleSubmissions(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                EmptyState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SampleSubmissions(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Your Photo Submissions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextGray
        )
        
        // Sample submission cards
        SubmissionCard(
            title = "Sunset Photography",
            description = "Beautiful sunset at the beach",
            votes = 15,
            views = 234,
            date = "2024-01-15"
        )
        
        SubmissionCard(
            title = "Mountain Landscape",
            description = "Majestic mountain peaks",
            votes = 23,
            views = 456,
            date = "2024-01-20"
        )
        
        SubmissionCard(
            title = "Urban Architecture",
            description = "Modern city buildings",
            votes = 8,
            views = 123,
            date = "2024-01-25"
        )
    }
}

@Composable
private fun SubmissionCard(
    title: String,
    description: String,
    votes: Int,
    views: Int,
    date: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Photo placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Photo",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Photo info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray.copy(alpha = 0.7f),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$votes votes",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "$views views",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray.copy(alpha = 0.6f)
                        )
                    }
                    
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotLoggedInState(
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
                    imageVector = Icons.Default.Photo,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Please Login",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "You need to be logged in to view your submissions",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextGray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
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
                    imageVector = Icons.Default.Photo,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No Submissions Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "You haven't submitted any photos yet. Start participating in contests!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextGray.copy(alpha = 0.7f)
                )
            }
        }
    }
}
