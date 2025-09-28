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
        
        // Debug: Log the original content
        println("DEBUG: Processing content with length: ${htmlContent.length}")
        println("DEBUG: Content preview: ${htmlContent.take(200)}")
        
        // Extract all images first to see what we're working with
        val allImages = extractImageUrls(htmlContent)
        println("DEBUG: Found ${allImages.size} images in content: $allImages")
        
        // Split by common HTML block elements
        val blocks = htmlContent.split(Regex("""</?(?:p|div|h[1-6]|br)\s*[^>]*>"""))
        println("DEBUG: Split into ${blocks.size} blocks")
        
        for ((index, block) in blocks.withIndex()) {
            val trimmedBlock = block.trim()
            if (trimmedBlock.isEmpty()) continue
            
            println("DEBUG: Processing block $index: '${trimmedBlock.take(100)}'")
            
            // Check if this block contains images
            val imgRegex = Regex("""<img[^>]+src\s*=\s*["']([^"']+)["'][^>]*>""")
            val imgMatches = imgRegex.findAll(trimmedBlock)
            
            if (imgMatches.any()) {
                println("DEBUG: Found ${imgMatches.count()} images in block $index")
                
                // Extract images first
                for (match in imgMatches) {
                    match.groupValues.getOrNull(1)?.let { imageUrl ->
                        if (imageUrl.isNotBlank()) {
                            println("DEBUG: Adding image element with URL: $imageUrl")
                            elements.add(ContentElement.Image(imageUrl))
                        } else {
                            println("DEBUG: Skipping empty image URL")
                        }
                    }
                }
                
                // Extract remaining text content and clean it properly
                val textContent = trimmedBlock
                    .replace(imgRegex, "") // Remove image tags
                    .replace(Regex("<[^>]*>"), "") // Remove any remaining HTML tags
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
                
                if (textContent.isNotEmpty()) {
                    println("DEBUG: Adding text element: '${textContent.take(50)}'")
                    elements.add(ContentElement.Text(textContent))
                }
            } else {
            // Just text content - clean it properly
                val cleanText = trimmedBlock
                    .replace(Regex("<[^>]*>"), "") // Remove HTML tags
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
                
                if (cleanText.isNotEmpty()) {
                    println("DEBUG: Adding text element: '${cleanText.take(50)}'")
                    elements.add(ContentElement.Text(cleanText))
                }
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
