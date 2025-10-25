package com.example.picchallenge.ui.blog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picchallenge.data.model.BlogPostDisplay
import com.example.picchallenge.ui.theme.*
import com.example.picchallenge.ui.viewmodel.BlogViewModel
import com.example.picchallenge.utils.NetworkResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogScreen(
    onNavigateBack: () -> Unit,
    onPostClick: (String) -> Unit = {},
    blogViewModel: BlogViewModel = hiltViewModel()
) {
    val blogPosts by blogViewModel.blogPosts.collectAsState()
    val isLoading by blogViewModel.isLoading.collectAsState()
    
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
                            "Blog Posts",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack, 
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
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
            when (val result = blogPosts) {
                is NetworkResult.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is NetworkResult.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(result.data) { post ->
                            BlogPostItem(
                                post = post,
                                onClick = { onPostClick(post.id.toString()) }
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    ErrorState(
                        message = result.message,
                        onRetry = { blogViewModel.refreshPosts() }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlogPostItem(
    post: BlogPostDisplay,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
                            // Featured Image (if available) - optimized for fast loading
                            post.featuredImageUrl?.let { imageUrl ->
                                com.example.picchallenge.ui.components.EnhancedImage(
                                    imageUrl = imageUrl,
                                    contentDescription = "Article thumbnail",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(bottom = 12.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
            
            // Title with better formatting
            Text(
                text = formatTitle(post.title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Excerpt with better formatting
            Text(
                text = formatExcerpt(post.excerpt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                            Text(
                                text = "Read More →",
                                style = MaterialTheme.typography.labelMedium,
                                color = PrimaryModern,
                                fontWeight = FontWeight.Medium
                            )
            }
        }
    }
}

private fun formatTitle(title: String): String {
    return title
        .replace("&#8216;", "'") // Left single quotation mark
        .replace("&#8217;", "'") // Right single quotation mark
        .replace("&#8220;", "\"") // Left double quotation mark
        .replace("&#8221;", "\"") // Right double quotation mark
        .replace("&#8230;", "...") // Ellipsis
        .replace("&amp;", "&") // Ampersand
        .replace("&lt;", "<") // Less than
        .replace("&gt;", ">") // Greater than
        .trim()
}

private fun formatExcerpt(excerpt: String): String {
    return excerpt
        .replace("&#8216;", "'") // Left single quotation mark
        .replace("&#8217;", "'") // Right single quotation mark
        .replace("&#8220;", "\"") // Left double quotation mark
        .replace("&#8221;", "\"") // Right double quotation mark
        .replace("&#8230;", "...") // Ellipsis
        .replace("&amp;", "&") // Ampersand
        .replace("&lt;", "<") // Less than
        .replace("&gt;", ">") // Greater than
        .trim()
        .takeIf { it.isNotEmpty() } ?: "Click to read the full article..."
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
                containerColor = MaterialTheme.colorScheme.surface
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
                    imageVector = Icons.AutoMirrored.Filled.Article,
                    contentDescription = null,
                    tint = StatusRed,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Error loading posts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
