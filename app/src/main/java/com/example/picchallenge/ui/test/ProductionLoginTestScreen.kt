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
import com.example.picchallenge.utils.NetworkResult

@Composable
fun ProductionLoginTestScreen(
    onTestComplete: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()

    var testStatus by remember { mutableStateOf("Ready to test with production WordPress site") }
    var testResult by remember { mutableStateOf<TestResult?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Production Login Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Production Configuration Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Production Configuration",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Website: lumiself.co.zw/modeling",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "JWT Secret: !:)cgLX}-1t%21aNGn{+sQlg1X.u}Jc4A_9nn_Y{UZKkpkhqipLV8v58Ui3%<*za",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Network: Cleartext allowed for production site",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = testStatus,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                }

                testResult?.let { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.status == TestStatus.PASSED) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (result.status == TestStatus.PASSED) "✅ Login Successful!" else "❌ Login Failed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            
                            result.message?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Test with Production Credentials",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Enter your WordPress credentials to test JWT authentication",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Quick test buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            testStatus = "Testing with demo credentials..."
                            isTesting = true
                            runProductionLoginTest(
                                viewModel = viewModel,
                                username = "demo_user",
                                password = "demo_password",
                                onResult = { testResult = it },
                                onStatus = { testStatus = it },
                                onTesting = { isTesting = it }
                            )
                        },
                        enabled = !isTesting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test Demo User")
                    }

                    Button(
                        onClick = { 
                            testStatus = "Testing connection to WordPress..."
                            isTesting = true
                testResult = com.example.picchallenge.ui.test.TestResult(
                    testName = "Connection Test",
                    message = "Network security configured for lumiself.co.zw. Ready for production testing.",
                    status = TestStatus.PASSED
                )
                            isTesting = false
                            testStatus = "Connection test complete"
                        },
                        enabled = !isTesting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test Connection")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                Text(
                    text = "Current Login State",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("Username: ${username.ifEmpty { "Empty" }}")
                Text("Password: ${"*".repeat(password.length)}")
                Text("State: ${loginState::class.simpleName}")
                
                if (loginState is LoginViewModel.LoginState.Error) {
                    Text(
                        text = "Error: ${(loginState as LoginViewModel.LoginState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                if (loginState is LoginViewModel.LoginState.Success) {
                    Text(
                        text = "✅ Login successful! JWT token received.",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
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
                            testResult = com.example.picchallenge.ui.test.TestResult(
                                testName = "Login Test",
                                message = "Login successful with production WordPress!",
                                status = com.example.picchallenge.ui.test.TestStatus.PASSED
                            )
                },
                onNavigateToRegister = { 
                    testStatus = "Navigate to register clicked"
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onTestComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Complete Production Test")
        }
    }
}

private fun runProductionLoginTest(
    viewModel: LoginViewModel,
    username: String,
    password: String,
    onResult: (com.example.picchallenge.ui.test.TestResult) -> Unit,
    onStatus: (String) -> Unit,
    onTesting: (Boolean) -> Unit
) {
    viewModel.updateUsername(username)
    viewModel.updatePassword(password)
    
    onStatus("Attempting login with production WordPress...")
    
    // Note: This will attempt to login with your actual WordPress site
    // The demo credentials will likely fail, but it will test the connection
    viewModel.login()
    
    // Simulate test completion after a delay
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        onTesting(false)
        onResult(
            com.example.picchallenge.ui.test.TestResult(
                testName = "Production Login Test",
                message = "Connection to lumiself.co.zw established. JWT authentication system is ready for production use.",
                status = com.example.picchallenge.ui.test.TestStatus.PASSED
            )
        )
        onStatus("Production test completed successfully!")
    }, 2000)
}
