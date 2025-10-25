package com.example.picchallenge.ui.blog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enhanced content display that supports images and rich formatting
 * Processes content to extract and display images properly
 */
@Composable
fun EnhancedArticleContent(
    content: String,
    featuredImageUrl: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Display featured image if available
        featuredImageUrl?.let { imageUrl ->
                            com.example.picchallenge.ui.components.EnhancedImage(
                                imageUrl = imageUrl,
                                contentDescription = "Featured image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
        }
        
        // Process and display content with images
        ContentWithImages(content = content)
    }
}

/**
 * Processes content to extract and display images inline with text
 * Enhanced with better spacing for improved readability
 */
@Composable
private fun ContentWithImages(
    content: String,
    modifier: Modifier = Modifier
) {
    val elements = remember(content) {
        ContentImageProcessor.processContentWithImages(content)
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        elements.forEachIndexed { index, element ->
            when (element) {
                is ContentElement.Text -> {
                    if (element.content.isNotBlank()) {
                        // Split text by paragraphs to maintain proper spacing
                        val paragraphs = element.content.split("\n\n").filter { it.isNotBlank() }
                        
                        paragraphs.forEach { paragraph ->
                        Text(
                            text = paragraph.trim(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                letterSpacing = 0.15.sp, // Slight letter spacing for better readability
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f), // Slightly softer color
                            lineHeight = 26.sp, // Slightly increased line height for better readability
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = if (index == 0) 0.dp else 12.dp, // Less space at top of first element
                                    bottom = 8.dp, // More space after paragraphs
                                    start = 4.dp, // Small horizontal padding
                                    end = 4.dp
                                )
                        )
                        }
                    }
                }
                is ContentElement.Image -> {
                    // Use EnhancedImage for fast loading like contest images with 3:4 aspect ratio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 16.dp, // More space around images
                                horizontal = 4.dp
                            )
                    ) {
                        com.example.picchallenge.ui.components.EnhancedImage(
                            imageUrl = element.url,
                            contentDescription = "Article image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f), // 3:4 aspect ratio
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

/**
 * Simple enhanced content display with better formatting but still very safe
 */
@Composable
private fun SimpleEnhancedContentText(
    content: String,
    modifier: Modifier = Modifier
) {
    val cleanContent = stripHtmlBasic(content)
    
    // Split content into paragraphs for better readability
    val paragraphs = cleanContent.split("\n\n").filter { it.isNotBlank() }
    
    Column(modifier = modifier.fillMaxWidth()) {
        paragraphs.forEach { paragraph ->
            Text(
                text = paragraph.trim(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

/**
 * Simple fallback content display that just strips HTML and shows plain text
 * This ensures the app never breaks even if the enhanced formatting fails
 */
@Composable
private fun SimpleContentText(
    content: String,
    modifier: Modifier = Modifier
) {
    val cleanContent = stripHtmlBasic(content)
    
    Text(
        text = cleanContent,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 24.sp,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Basic HTML stripping that's very safe and won't cause issues
 */
private fun stripHtmlBasic(html: String): String {
    return html
        .replace(Regex("<.*?>"), "") // Remove HTML tags, replace with space
        .replace("&#8216;", "'") // Left single quotation mark
        .replace("&#8217;", "'") // Right single quotation mark
        .replace("&#8220;", "\"") // Left double quotation mark
        .replace("&#8221;", "\"") // Right double quotation mark
        .replace("&#8230;", "...") // Ellipsis
        .replace("&amp;", "&") // Ampersand
        .replace("&lt;", "<") // Less than
        .replace("&gt;", ">") // Greater than
        .replace("&nbsp;", " ") // Non-breaking space
        .replace(Regex("\\s+"), " ") // Collapse multiple spaces
        .trim()
}
