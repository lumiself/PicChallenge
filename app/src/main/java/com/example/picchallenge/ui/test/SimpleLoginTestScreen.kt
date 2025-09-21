package com.example.picchallenge.ui.test

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picchallenge.ui.auth.LoginScreen
import com.example.picchallenge.ui.viewmodel.LoginViewModel

@Composable
fun SimpleLoginTestScreen(
    onTestComplete: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login Screen Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Current State Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Current State: ${loginState::class.simpleName}")
                Text("Username: ${username.ifEmpty { "Empty" }}")
                Text("Password: ${"*".repeat(password.length)}")
                
                if (loginState is LoginViewModel.LoginState.Error) {
                    Text(
                        text = "Error: ${(loginState as LoginViewModel.LoginState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                if (loginState is LoginViewModel.LoginState.Success) {
                    Text(
                        text = "Login Successful! Token received.",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { 
                    viewModel.updateUsername("test@example.com")
                    viewModel.updatePassword("password123")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Fill Test Data")
            }

            Button(
                onClick = { viewModel.resetState() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Screen Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LoginScreen(
                onLoginSuccess = { 
                    // Handle success
                },
                onNavigateToRegister = { 
                    // Handle navigation
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onTestComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Complete Test")
        }
    }
}
