package com.example.picchallenge.data.model

/**
 * Represents the download state of images for a contest
 */
data class ImageDownloadState(
    val contestId: Int,
    val totalImages: Int = 0,
    val downloadedImages: Int = 0,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadedImageUrls: Map<String, String> = emptyMap(), // Maps original URL to cached/local path
    val errors: List<String> = emptyList()
) {
    val progressText: String
        get() = "$downloadedImages/$totalImages images downloaded"
    
    val isComplete: Boolean
        get() = downloadedImages >= totalImages && totalImages > 0
    
    val progressPercentage: Int
        get() = if (totalImages > 0) ((downloadedImages.toFloat() / totalImages) * 100).toInt() else 0
}

/**
 * Represents the state of a single image download
 */
data class SingleImageDownloadState(
    val originalUrl: String,
    val localPath: String? = null,
    val isDownloading: Boolean = false,
    val isDownloaded: Boolean = false,
    val error: String? = null
)
