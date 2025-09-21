package com.example.picchallenge.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picchallenge.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()

    // Handle login success
    LaunchedEffect(loginState) {
        if (loginState is LoginViewModel.LoginState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Title
        Text(
            text = "PicChallenge",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Welcome Back",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Sign in to continue",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = { viewModel.updateUsername(it) },
            label = { Text("Username or Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            enabled = loginState !is LoginViewModel.LoginState.Loading
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            enabled = loginState !is LoginViewModel.LoginState.Loading
        )

        // Remember Me Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { viewModel.updateRememberMe(it) },
                enabled = loginState !is LoginViewModel.LoginState.Loading
            )
            Text(
                text = "Remember me",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Login Button
        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = loginState !is LoginViewModel.LoginState.Loading &&
                     username.isNotBlank() && 
                     password.isNotBlank()
        ) {
            if (loginState is LoginViewModel.LoginState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Error Message
        if (loginState is LoginViewModel.LoginState.Error) {
            Text(
                text = (loginState as LoginViewModel.LoginState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Register Link
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onNavigateToRegister,
                enabled = loginState !is LoginViewModel.LoginState.Loading
            ) {
                Text(
                    text = "Sign Up",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LoginContent(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LoginScreen(
            onLoginSuccess = onLoginSuccess,
            onNavigateToRegister = onNavigateToRegister
        )
    }
}
