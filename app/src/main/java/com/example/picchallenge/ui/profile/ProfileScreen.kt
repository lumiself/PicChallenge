package com.example.picchallenge.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
fun ProfileScreen(
    onLoginClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onUploadPhoto: (Int) -> Unit,
    onViewSubmissions: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    // Simple login state - assume not logged in for demo
    var isLoggedIn by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Profile",
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
            if (isLoggedIn) {
                LoggedInProfile(
                    onLogout = { isLoggedIn = false },
                    onUploadPhoto = onUploadPhoto,
                    onViewSubmissions = onViewSubmissions
                )
            } else {
                GuestProfile(
                    onLoginClick = { isLoggedIn = true }
                )
            }
        }
    }
}

@Composable
private fun LoggedInProfile(
    onLogout: () -> Unit,
    onUploadPhoto: (Int) -> Unit,
    onViewSubmissions: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Header
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "You're logged in and ready to participate in photo contests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Quick Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardWhite
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Button(
                    onClick = { onUploadPhoto(1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Upload Photo to Contest")
                }
                
                OutlinedButton(
                    onClick = onViewSubmissions,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View My Submissions")
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StatusRed
            )
        ) {
            Text("Logout")
        }
    }
}

@Composable
private fun GuestProfile(
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Message
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Welcome to PicChallenge",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Join our photo contests and showcase your photography skills",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Registration Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardWhite
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How to Join",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = "Registration is currently handled manually to ensure quality participants. Here's how to join:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = "1. Contact us via WhatsApp or email\n2. Provide your basic information\n3. Wait for approval\n4. Start participating!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Contact: +263 782 684 837\nEmail: modeling@lumiself.co.zw",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Login to Your Account")
        }
    }
}
