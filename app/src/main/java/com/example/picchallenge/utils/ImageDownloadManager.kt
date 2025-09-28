package com.example.picchallenge.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.picchallenge.data.model.ImageDownloadState
import com.example.picchallenge.data.model.Photo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages background downloading and caching of contest images
 */
@Singleton
class ImageDownloadManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "ImageDownloadManager"
        private const val CACHE_DIR_NAME = "contest_images"
        private const val MAX_CONCURRENT_DOWNLOADS = 3
    }

    private val downloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val imageCacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    // Track download states for different contests
    private val downloadStates = mutableMapOf<Int, MutableStateFlow<ImageDownloadState>>()

    /**
     * Start downloading all images for a contest
     */
    fun startDownloadingContestImages(
        contestId: Int,
        photos: List<Photo>
    ): StateFlow<ImageDownloadState> {
        val stateFlow = MutableStateFlow(
            ImageDownloadState(
                contestId = contestId,
                totalImages = photos.size,
                isDownloading = true
            )
        )
        
        downloadStates[contestId] = stateFlow

        downloadScope.launch {
            downloadAllImages(contestId, photos, stateFlow)
        }

        return stateFlow.asStateFlow()
    }

    /**
     * Get the current download state for a contest
     */
    fun getDownloadState(contestId: Int): StateFlow<ImageDownloadState>? {
        return downloadStates[contestId]?.asStateFlow()
    }

    /**
     * Check if an image is already downloaded and cached
     */
    fun getCachedImagePath(originalUrl: String): String? {
        val cachedFile = getCachedFile(originalUrl)
        return if (cachedFile.exists()) {
            cachedFile.absolutePath
        } else {
            null
        }
    }

    /**
     * Clear cached images for a specific contest
     */
    fun clearContestCache(contestId: Int) {
        downloadScope.launch {
            try {
                val contestDir = File(imageCacheDir, "contest_$contestId")
                if (contestDir.exists()) {
                    contestDir.deleteRecursively()
                }
                downloadStates.remove(contestId)
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cache for contest $contestId", e)
            }
        }
    }

    /**
     * Clear all cached images
     */
    fun clearAllCache() {
        downloadScope.launch {
            try {
                imageCacheDir.deleteRecursively()
                imageCacheDir.mkdirs()
                downloadStates.clear()
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing all cache", e)
            }
        }
    }

    private suspend fun downloadAllImages(
        contestId: Int,
        photos: List<Photo>,
        stateFlow: MutableStateFlow<ImageDownloadState>
    ) {
        val downloadedUrls = mutableMapOf<String, String>()
        val errors = mutableListOf<String>()
        var downloadedCount = 0

        // Get all unique image URLs from photos
        val imageUrls = photos.flatMap { photo ->
            listOfNotNull(
                photo.thumbnail,
                photo.medium,
                photo.large,
                photo.url
            )
        }.distinct()

        // Download images with limited concurrency
        coroutineScope {
            // Use chunked to limit concurrency
            val chunks = imageUrls.chunked(MAX_CONCURRENT_DOWNLOADS)
            
            for (chunk in chunks) {
                val results = chunk.map { imageUrl ->
                    async {
                        try {
                            val cachedPath = downloadImage(imageUrl, contestId)
                            if (cachedPath != null) {
                                downloadedCount++
                                
                                // Update progress
                                stateFlow.value = stateFlow.value.copy(
                                    downloadedImages = downloadedCount,
                                    downloadProgress = downloadedCount.toFloat() / imageUrls.size,
                                    downloadedImageUrls = downloadedUrls + mapOf(imageUrl to cachedPath)
                                )
                                
                                Log.d(TAG, "Downloaded image $downloadedCount/${imageUrls.size}: $imageUrl")
                                imageUrl to cachedPath
                            } else {
                                errors.add("Failed to download: $imageUrl")
                                null
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error downloading image: $imageUrl", e)
                            errors.add("Error downloading $imageUrl: ${e.message}")
                            null
                        }
                    }
                }.awaitAll()
                
                // Update downloaded URLs
                results.filterNotNull().forEach { (url, path) ->
                    downloadedUrls[url] = path
                }
            }
        }

        // Final state update
        stateFlow.value = stateFlow.value.copy(
            isDownloading = false,
            downloadProgress = 1f,
            downloadedImages = downloadedCount,
            errors = errors
        )

        Log.d(TAG, "Download completed for contest $contestId. Downloaded: $downloadedCount/${imageUrls.size}")
    }

    private suspend fun downloadImage(imageUrl: String, contestId: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Check if already cached
                getCachedImagePath(imageUrl)?.let { return@withContext it }

                val url = URL(imageUrl)
                val connection = url.openConnection().apply {
                    connectTimeout = 10000 // 10 seconds
                    readTimeout = 30000 // 30 seconds
                }

                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap != null) {
                    return@withContext saveImageToCache(bitmap, imageUrl, contestId)
                } else {
                    Log.e(TAG, "Failed to decode bitmap from: $imageUrl")
                    return@withContext null
                }
            } catch (e: IOException) {
                Log.e(TAG, "IO Error downloading image: $imageUrl", e)
                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error downloading image: $imageUrl", e)
                return@withContext null
            }
        }
    }

    private fun saveImageToCache(bitmap: Bitmap, originalUrl: String, contestId: Int): String? {
        return try {
            val fileName = getFileNameFromUrl(originalUrl)
            val contestDir = File(imageCacheDir, "contest_$contestId").apply {
                if (!exists()) mkdirs()
            }
            
            val cachedFile = File(contestDir, fileName)
            
            FileOutputStream(cachedFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
            }
            
            Log.d(TAG, "Saved image to cache: ${cachedFile.absolutePath}")
            cachedFile.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error saving image to cache", e)
            null
        }
    }

    private fun getCachedFile(originalUrl: String): File {
        val fileName = getFileNameFromUrl(originalUrl)
        return File(imageCacheDir, fileName)
    }

    private fun getFileNameFromUrl(url: String): String {
        return url.substringAfterLast('/').takeIf { it.isNotEmpty() } ?: "image_${url.hashCode()}.jpg"
    }
}
