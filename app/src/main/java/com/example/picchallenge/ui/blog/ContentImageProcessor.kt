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
     * Processes HTML content to preserve structure while handling images
     */
    fun processContentWithImages(htmlContent: String): List<ContentElement> {
        val elements = mutableListOf<ContentElement>()
        
        // Split by common HTML block elements
        val blocks = htmlContent.split(Regex("""</?(?:p|div|h[1-6]|br)\s*[^>]*>"""))
        
        for (block in blocks) {
            val trimmedBlock = block.trim()
            if (trimmedBlock.isEmpty()) continue
            
            // Check if this block contains images
            val imgRegex = Regex("""<img[^>]+src\s*=\s*["']([^"']+)["'][^>]*>""")
            val imgMatches = imgRegex.findAll(trimmedBlock)
            
            if (imgMatches.any()) {
                // Extract images first
                for (match in imgMatches) {
                    match.groupValues.getOrNull(1)?.let { imageUrl ->
                        if (imageUrl.isNotBlank()) {
                            elements.add(ContentElement.Image(imageUrl))
                        }
                    }
                }
                
                // Extract remaining text content
                val textContent = trimmedBlock.replace(imgRegex, "").trim()
                if (textContent.isNotEmpty()) {
                    elements.add(ContentElement.Text(textContent))
                }
            } else {
                // Just text content
                elements.add(ContentElement.Text(trimmedBlock))
            }
        }
        
        return elements
    }
    
    /**
     * Strips HTML tags but preserves image URLs for separate processing
     */
    fun stripHtmlPreserveImages(html: String): String {
        return html
            .replace(Regex("<(?!img)[^>]*>"), "") // Remove all tags except img
            .replace(Regex("""<img[^>]*>"""), "") // Remove img tags but keep their content for separate processing
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
