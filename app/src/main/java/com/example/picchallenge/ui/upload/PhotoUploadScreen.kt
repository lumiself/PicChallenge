package com.example.picchallenge.ui.upload

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picchallenge.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoUploadScreen(
    contestId: Int,
    onUploadComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Upload Photo",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload Form
            UploadForm(
                title = title,
                onTitleChange = { title = it },
                description = description,
                onDescriptionChange = { description = it },
                isUploading = isUploading
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Upload Button
            Button(
                onClick = {
                    isUploading = true
                    // Simulate upload
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(2000)
                        onUploadComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                enabled = title.isNotBlank() && !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Upload",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Photo")
                }
            }
        }
    }
}

@Composable
private fun UploadForm(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isUploading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Photo Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Photo Selection Area
            PhotoSelectionArea(
                isUploading = isUploading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Photo Title") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                enabled = !isUploading,
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Contest Info
            ContestInfo()
        }
    }
}

@Composable
private fun PhotoSelectionArea(
    isUploading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.3f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Select Photo",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tap to select photo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
                
                Text(
                    text = "JPG, PNG formats supported",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ContestInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundLightGreen
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Contest Guidelines",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "• Maximum 3 photos per contest\n• Photos must be your original work\n• Follow contest theme and rules\n• Respect copyright and privacy",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray.copy(alpha = 0.8f)
            )
        }
    }
}

// Simple coroutine extension for demo
private fun kotlinx.coroutines.GlobalScope.launch(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
    // This is a simplified version for demo purposes
    // In a real app, you'd use proper coroutine scope
}
