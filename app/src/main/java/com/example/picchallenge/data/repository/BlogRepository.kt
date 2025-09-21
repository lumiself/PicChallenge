package com.example.picchallenge.data.repository

import com.example.picchallenge.data.model.BlogPostDisplay
import com.example.picchallenge.data.model.WordPressPost
import com.example.picchallenge.data.remote.WordPressApiService
import com.example.picchallenge.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
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
        return BlogPostDisplay(
            id = post.id,
            title = post.title.rendered,
            excerpt = stripHtml(post.excerpt.rendered),
            content = post.content.rendered,
            date = formatDate(post.date),
            link = post.link,
            featuredMediaId = post.featuredMedia,
            authorId = post.author
        )
    }
    
    private fun stripHtml(html: String): String {
        return html.replace(Regex("<.*?>"), "").trim()
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
