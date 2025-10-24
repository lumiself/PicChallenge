package com.example.picchallenge.ui.blog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

/**
 * Processes content to extract and display images properly
 */
object ContentImageProcessor {
    
    /**
     * Extracts image URLs from HTML content
     */
    fun extractImageUrls(htmlContent: String): List<String> {
        val imageUrls = mutableListOf<String>()
        val imgRegex = Regex("""<img[^>]+src\s*=\s*["']([^"']+)["'][^>]*>""")
        
        imgRegex.findAll(htmlContent).forEach { match ->
            match.groupValues.getOrNull(1)?.let { url ->
                if (url.isNotBlank()) {
                    imageUrls.add(url)
                }
            }
        }
        
        return imageUrls
    }
    
    /**
     * Processes HTML content to preserve original order while handling images
     * This version processes content sequentially without splitting or reordering
     */
    fun processContentWithImages(htmlContent: String): List<ContentElement> {
        val elements = mutableListOf<ContentElement>()
        
        // Debug: Log the original content
        println("DEBUG: Processing content with length: ${htmlContent.length}")
        println("DEBUG: Content preview: ${htmlContent.take(200)}")
        
        // Process HTML entities first
        var processedContent = preprocessHtmlEntities(htmlContent)
        
        // Find all image positions
        val imgRegex = Regex("""<img[^>]+src\s*=\s*["']([^"']+)["'][^>]*>""")
        val imageMatches = imgRegex.findAll(processedContent).toList()
        
        if (imageMatches.isEmpty()) {
            // No images, just process as text
            val cleanText = stripHtmlTags(processedContent)
            if (cleanText.isNotBlank()) {
                elements.add(ContentElement.Text(cleanText))
            }
            return elements
        }
        
        // Process content sequentially to maintain original order
        var currentPosition = 0
        val processedImageUrls = mutableSetOf<String>() // Track processed images to avoid duplicates
        
        for (match in imageMatches) {
            // Process text before this image
            if (match.range.first > currentPosition) {
                val textBefore = processedContent.substring(currentPosition, match.range.first)
                val cleanText = stripHtmlTags(textBefore)
                if (cleanText.isNotBlank()) {
                    println("DEBUG: Adding text element: '${cleanText.take(50)}'")
                    elements.add(ContentElement.Text(cleanText))
                }
            }
            
            // Process this image (avoid duplicates)
            val imageUrl = match.groupValues.getOrNull(1)
            if (imageUrl != null && imageUrl.isNotBlank() && !processedImageUrls.contains(imageUrl)) {
                println("DEBUG: Adding image element with URL: $imageUrl")
                elements.add(ContentElement.Image(imageUrl))
                processedImageUrls.add(imageUrl)
            }
            
            currentPosition = match.range.last + 1
        }
        
        // Process remaining text after last image
        if (currentPosition < processedContent.length) {
            val textAfter = processedContent.substring(currentPosition)
            val cleanText = stripHtmlTags(textAfter)
            if (cleanText.isNotBlank()) {
                println("DEBUG: Adding final text element: '${cleanText.take(50)}'")
                elements.add(ContentElement.Text(cleanText))
            }
        }
        
        println("DEBUG: Final elements count: ${elements.size}")
        elements.forEach { element ->
            when (element) {
                is ContentElement.Image -> println("DEBUG: Image element: ${element.url}")
                is ContentElement.Text -> println("DEBUG: Text element: '${element.content.take(50)}'")
            }
        }
        
        return elements
    }
    
    /**
     * Preprocess HTML entities
     */
    private fun preprocessHtmlEntities(html: String): String {
        return html
            .replace("&#8216;", "'") // Left single quotation mark
            .replace("&#8217;", "'") // Right single quotation mark
            .replace("&#8220;", "\"") // Left double quotation mark
            .replace("&#8221;", "\"") // Right double quotation mark
            .replace("&#8230;", "...") // Ellipsis
            .replace("&amp;", "&") // Ampersand
            .replace("&lt;", "<") // Less than
            .replace("&gt;", ">") // Greater than
            .replace("&nbsp;", " ") // Non-breaking space
            .replace("&quot;", "\"") // Quote
            .replace("&#39;", "'") // Apostrophe
            .replace("&#8211;", "–") // En dash
            .replace("&#8212;", "—") // Em dash
    }
    
    /**
     * Strip HTML tags while preserving text content
     */
    private fun stripHtmlTags(html: String): String {
        return html
            .replace(Regex("<[^>]*>"), " ") // Remove HTML tags
            .replace(Regex("\\s+"), " ") // Collapse multiple spaces
            .trim()
    }
    
    /**
     * Strips HTML tags but preserves image URLs for separate processing
     */
    fun stripHtmlPreserveImages(html: String): String {
        return html
            .replace(Regex("<(?!img)[^>]*>"), "") // Remove all tags except img
            .replace(Regex("""<img[^>]*>"""), "") // Remove img tags but keep their content for separate processing
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
}

/**
 * Represents different types of content elements
 */
sealed class ContentElement {
    data class Text(val content: String) : ContentElement()
    data class Image(val url: String) : ContentElement()
}

/**
 * Composable for displaying article images with proper loading states
 * Uses the same optimized approach as EnhancedImage for consistency
 */
@Composable
fun ArticleImage(
    imageUrl: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }
    
    val painter = rememberAsyncImagePainter(model = imageRequest)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator()
            }
            is AsyncImagePainter.State.Error -> {
                // Show placeholder or error state
                Text(
                    text = "Image failed to load",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                // Empty state or other states
            }
        }
    }
}
