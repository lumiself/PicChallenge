package com.example.picchallenge.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picchallenge.ui.viewmodel.SettingsViewModel
import com.example.picchallenge.utils.NetworkResult

@Composable
fun UrlConfigurationScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onUrlConfigured: () -> Unit = {}
) {
    val urlInput by settingsViewModel.urlInput.collectAsState()
    val isValidUrl by settingsViewModel.isValidUrl.collectAsState()
    val isLoading by settingsViewModel.isLoading.collectAsState()
    val saveResult by settingsViewModel.saveResult.collectAsState()
    val isFirstRun by settingsViewModel.isFirstRun.collectAsState()

    LaunchedEffect(saveResult) {
        if (saveResult is NetworkResult.Success) {
            onUrlConfigured()
            settingsViewModel.resetSaveResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🎯 Configure Your WordPress Site",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Enter your WordPress site URL to connect to your photo contest backend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // URL Input Field
        OutlinedTextField(
            value = urlInput,
            onValueChange = { settingsViewModel.updateUrlInput(it) },
            label = { Text("WordPress Site URL") },
            placeholder = { Text("example.com or 192.168.1.100") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = if (isValidUrl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            trailingIcon = {
                if (urlInput.isNotBlank()) {
                    Icon(
                        imageVector = if (isValidUrl) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isValidUrl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = urlInput.isNotBlank() && !isValidUrl,
            supportingText = {
                if (urlInput.isNotBlank() && !isValidUrl) {
                    Text(
                        text = "Please enter a valid URL (e.g., example.com or http://192.168.1.100)",
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (urlInput.isNotBlank() && isValidUrl) {
                    Text(
                        text = "Valid URL format",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // URL Format Examples
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Examples:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• example.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• http://192.168.1.100",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• https://mywordpresssite.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!isFirstRun) {
                OutlinedButton(
                    onClick = { settingsViewModel.dismissFirstRun() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Skip")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = { settingsViewModel.saveUrl() },
                modifier = Modifier.weight(1f),
                enabled = isValidUrl && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Continue")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Result
        saveResult?.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "URL saved successfully!",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = result.message ?: "Failed to save URL",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
