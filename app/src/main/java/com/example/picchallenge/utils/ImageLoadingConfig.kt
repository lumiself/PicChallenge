package com.example.picchallenge.utils

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoadingConfig @Inject constructor(
    private val context: Context
) {

    fun createEnhancedImageLoader(okHttpClient: OkHttpClient): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, "image_cache"))
                    .maxSizeBytes(250 * 1024 * 1024) // 250MB disk cache
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .crossfade(300) // 300ms crossfade duration
            .okHttpClient(okHttpClient)
            .respectCacheHeaders(true)
            .logger(DebugLogger()) // Remove in production
            .build()
    }

    companion object {
        // Image size presets for different use cases
        const val THUMBNAIL_SIZE = 150
        const val PREVIEW_SIZE = 400
        const val FULL_SIZE = 800
        
        // Memory cache configuration
        const val MEMORY_CACHE_SIZE_PERCENT = 0.25 // 25% of available memory
        
        // Disk cache configuration
        const val DISK_CACHE_SIZE_MB = 250L // 250MB
        const val DISK_CACHE_DIRECTORY = "image_cache"
        
        // Network configuration
        const val NETWORK_TIMEOUT_SECONDS = 30L
        const val CROSSFADE_DURATION_MS = 300
    }
}
