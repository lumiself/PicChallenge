package com.example.picchallenge.data.repository

import com.example.picchallenge.data.model.BlogPostDisplay
import com.example.picchallenge.data.model.WordPressPost
import com.example.picchallenge.data.remote.WordPressApiService
import com.example.picchallenge.ui.blog.ContentImageProcessor
import com.example.picchallenge.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlogRepository @Inject constructor(
    private val apiService: WordPressApiService
) {
    
    // News category ID from your WordPress site
    private val NEWS_CATEGORY_ID = 31
    
    suspend fun getNewsPosts(
        page: Int = 1,
        perPage: Int = 10
    ): NetworkResult<List<BlogPostDisplay>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPostsByCategory(
                    categoryId = NEWS_CATEGORY_ID,
                    perPage = perPage,
                    page = page
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { posts ->
                        val displayPosts = posts.map { post ->
                            convertToDisplayModel(post)
                        }
                        NetworkResult.Success(displayPosts)
                    } ?: NetworkResult.Error("Empty response body")
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "WordPress API endpoint not found. Check your site URL."
                        401 -> "Authentication failed."
                        500 -> "Server error. Check WordPress logs."
                        else -> "API Error: ${response.code()} - ${response.message()}"
                    }
                    NetworkResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "Cannot connect to server. Check your URL and internet connection."
                    is java.net.SocketTimeoutException -> "Connection timeout. Server may be down or slow."
                    is java.net.ConnectException -> "Connection refused. Check if server is running."
                    else -> "Network error: ${e.message ?: "Unknown error"}"
                }
                NetworkResult.Error(errorMessage)
            }
        }
    }
    
    suspend fun getAllPosts(
        page: Int = 1,
        perPage: Int = 10
    ): NetworkResult<List<BlogPostDisplay>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPosts(
                    perPage = perPage,
                    page = page
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { posts ->
                        val displayPosts = posts.map { post ->
                            convertToDisplayModel(post)
                        }
                        NetworkResult.Success(displayPosts)
                    } ?: NetworkResult.Error("Empty response body")
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "WordPress API endpoint not found. Check your site URL."
                        401 -> "Authentication failed."
                        500 -> "Server error. Check WordPress logs."
                        else -> "API Error: ${response.code()} - ${response.message()}"
                    }
                    NetworkResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "Cannot connect to server. Check your URL and internet connection."
                    is java.net.SocketTimeoutException -> "Connection timeout. Server may be down or slow."
                    is java.net.ConnectException -> "Connection refused. Check if server is running."
                    else -> "Network error: ${e.message ?: "Unknown error"}"
                }
                NetworkResult.Error(errorMessage)
            }
        }
    }
    
    private fun convertToDisplayModel(post: WordPressPost): BlogPostDisplay {
        val contentImageUrls = ContentImageProcessor.extractImageUrls(post.content.rendered)
        val featuredImageUrl = extractFeaturedImageUrl(post)
        
        return BlogPostDisplay(
            id = post.id,
            title = formatHtmlContent(post.title.rendered),
            excerpt = formatExcerptContent(post.excerpt.rendered),
            content = processContentForDisplay(post.content.rendered),
            date = formatDate(post.date),
            link = post.link,
            featuredMediaId = post.featuredMedia,
            featuredImageUrl = featuredImageUrl,
            contentImageUrls = contentImageUrls,
            authorId = post.author
        )
    }
    
    private fun extractFeaturedImageUrl(post: WordPressPost): String? {
        // Try to extract the first image from the content as featured image
        // This is more reliable than trying to construct fake URLs
        val contentImages = ContentImageProcessor.extractImageUrls(post.content.rendered)
        return contentImages.firstOrNull()
    }
    
    private fun processContentForDisplay(html: String): String {
        // Use the new processor that preserves image structure
        return ContentImageProcessor.stripHtmlPreserveImages(html)
    }
    
    private fun formatHtmlContent(html: String): String {
        return html
            .replace(Regex("<.*?>"), "") // Remove HTML tags
            .replace("&#8217;", "'") // Right single quotation mark
            .replace("&#8220;", "\"") // Left double quotation mark
            .replace("&#8221;", "\"") // Right double quotation mark
            .replace("&#8230;", "...") // Ellipsis
            .replace("&#8211;", "–") // En dash
            .replace("&#8212;", "—") // Em dash
            .replace("&amp;", "&") // Ampersand
            .replace("&lt;", "<") // Less than
            .replace("&gt;", ">") // Greater than
            .replace("&nbsp;", " ") // Non-breaking space
            .replace(Regex("\\s+"), " ") // Collapse multiple spaces
            .trim()
    }
    
    private fun formatExcerptContent(html: String): String {
        val cleanText = formatHtmlContent(html)
        return if (cleanText.length > 150) {
            cleanText.take(150) + "..."
        } else {
            cleanText
        }
    }
    
    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString.take(10) // Fallback to just the date part
        }
    }
}
