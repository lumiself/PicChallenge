package com.example.picchallenge.data.remote

import com.example.picchallenge.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtAuthInterceptor @Inject constructor(
    private val authRepository: AuthRepository
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Don't add auth header for login/register endpoints
        if (shouldSkipAuthentication(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        // Get current token
        val token = authRepository.getToken()
        
        // If no token, proceed without authentication
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Check if token needs refresh
        if (authRepository.isTokenExpired()) {
            val refreshSuccess = runBlocking {
                authRepository.refreshTokenIfNeeded()
            }
            
            if (!refreshSuccess) {
                // Token refresh failed, proceed without auth or throw exception
                return chain.proceed(originalRequest)
            }
        }

        // Build authenticated request
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(authenticatedRequest)

        // Handle 401 Unauthorized responses
        if (response.code == 401) {
            response.close()
            
            // Try to refresh token
            val refreshSuccess = runBlocking {
                authRepository.refreshTokenIfNeeded()
            }
            
            if (refreshSuccess) {
                // Retry with new token
                val newToken = authRepository.getToken()
                if (!newToken.isNullOrEmpty()) {
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                    return chain.proceed(retryRequest)
                }
            }
            
            // Refresh failed, logout user
            runBlocking {
                authRepository.logout()
            }
        }

        return response
    }

    private fun shouldSkipAuthentication(request: Request): Boolean {
        val path = request.url.encodedPath
        val skipPaths = listOf(
            "/token",
            "/register",
            "/forgot-password",
            "/contests", // Public contest listing
            "/photos/popular", // Public photos
            "/photos/recent", // Public photos
            "/search" // Public search
        )
        
        return skipPaths.any { path.contains(it) }
    }
}
