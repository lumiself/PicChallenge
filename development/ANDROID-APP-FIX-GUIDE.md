# Android App Registration/Login Fix Guide

## 🚨 **CRITICAL ISSUES IDENTIFIED**

Your Android app registration and login are not working because of **fundamental API integration problems**. Here's the complete analysis and solution.

## 📋 **Problem Summary**

### **❌ Issue 1: Wrong API Endpoints**
Your app is using **outdated WordPress endpoints** instead of your new JWT production endpoints:

| Current (Wrong) | Should Be (Correct) |
|-----------------|---------------------|
| `wp-json/jwt-auth/v1/token` | `jwt-um/v1/login` |
| `wp-json/wp/v2/users/register` | `jwt-um/v1/register` |

### **❌ Issue 2: Incorrect Response Models**
Your response models don't match the production API responses.

### **❌ Issue 3: Missing JWT Token Support**
Your app doesn't properly handle JWT tokens for authentication.

## 🎯 **Complete Solution**

### **Step 1: Create New Authentication Service**

Create a new file: `app/src/main/java/com/example/picchallenge/data/remote/JWTAuthApiService.kt`

```kotlin
package com.example.picchallenge.data.remote

import com.example.picchallenge.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface JWTAuthApiService {
    
    /**
     * Login with username/email and password
     * Returns JWT token and user data
     */
    @POST("jwt-um/v1/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<JWTLoginResponse>

    /**
     * Register new user
     * No email verification required - instant login
     */
    @POST("jwt-um/v1/register")
    suspend fun registerUser(@Body request: JWTRegisterRequest): Response<JWTRegisterResponse>

    /**
     * Get current user profile (requires auth)
     */
    @GET("jwt-um/v1/user/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<JWTProfileResponse>

    /**
     * Check if user can vote in contest (requires auth)
     */
    @GET("jwt-um/v1/user/can-vote")
    suspend fun canUserVote(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int
    ): Response<CanVoteResponse>
}
```

### **Step 2: Create New Request/Response Models**

Update `app/src/main/java/com/example/picchallenge/data/model/Auth.kt`:

```kotlin
package com.example.picchallenge.data.model

import com.google.gson.annotations.SerializedName

/**
 * JWT Authentication models for production API
 */

// Login Request (same as before)
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// NEW: JWT Login Response
data class JWTLoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: JWTUser,
    @SerializedName("message") val message: String
)

data class JWTUser(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("avatar") val avatar: String?
)

// NEW: JWT Register Request (same as before, but response is different)
data class JWTRegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("first_name") val firstName: String = "",
    @SerializedName("last_name") val lastName: String = ""
)

// NEW: JWT Register Response
data class JWTRegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("message") val message: String,
    @SerializedName("verification_required") val verificationRequired: Boolean
)

// NEW: Profile Response
data class JWTProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("user") val user: JWTUser
)

// NEW: Can Vote Response
data class CanVoteResponse(
    @SerializedName("can_vote") val canVote: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("reason") val reason: String? = null
)
```

### **Step 3: Update Network Module**

Create `app/src/main/java/com/example/picchallenge/di/JWTAuthNetworkModule.kt`:

```kotlin
package com.example.picchallenge.di

import com.example.picchallenge.data.remote.JWTAuthApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JWTAuthNetworkModule {

    @Provides
    @Singleton
    @JWTAuthRetrofit
    fun provideJWTAuthRetrofit(
        okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository
    ): Retrofit {
        val baseUrl = runBlocking {
            val mainUrl = settingsRepository.baseUrl.first()
            val cleanUrl = mainUrl.removeSuffix("/").removeSuffix("wp-json").removeSuffix("/")
            "$cleanUrl/wp-json/"
        }
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideJWTAuthApiService(
        @JWTAuthRetrofit retrofit: Retrofit
    ): JWTAuthApiService {
        return retrofit.create(JWTAuthApiService::class.java)
    }
}

// Qualifier for JWT Auth Retrofit
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class JWTAuthRetrofit
```

### **Step 4: Update AuthViewModel**

Replace your `AuthViewModel.kt` with this corrected version:

```kotlin
package com.example.picchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchallenge.data.model.*
import com.example.picchallenge.data.remote.JWTAuthApiService
import com.example.picchallenge.utils.NetworkResult
import com.example.picchallenge.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val jwtAuthApiService: JWTAuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginState = MutableStateFlow<NetworkResult<JWTLoginResponse>>(NetworkResult.Loading)
    val loginState: StateFlow<NetworkResult<JWTLoginResponse>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<NetworkResult<JWTRegisterResponse>>(NetworkResult.Loading)
    val registerState: StateFlow<NetworkResult<JWTRegisterResponse>> = _registerState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            if (tokenManager.isAuthenticated()) {
                _authState.value = AuthState.Authenticated(
                    userId = tokenManager.getUserId(),
                    userEmail = tokenManager.getUserEmail() ?: "",
                    userDisplayName = tokenManager.getUserDisplayName() ?: ""
                )
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = NetworkResult.Loading
            
            try {
                val response = jwtAuthApiService.loginUser(LoginRequest(username, password))
                
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    
                    // Save token securely
                    tokenManager.saveToken(
                        token = loginResponse.token,
                        userId = loginResponse.user.id,
                        userEmail = loginResponse.user.email,
                        userDisplayName = loginResponse.user.displayName
                    )
                    
                    // Update auth state
                    _authState.value = AuthState.Authenticated(
                        userId = loginResponse.user.id,
                        userEmail = loginResponse.user.email,
                        userDisplayName = loginResponse.user.displayName
                    )
                    
                    _loginState.value = NetworkResult.Success(loginResponse)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid username or password"
                        403 -> "Access denied"
                        404 -> "Login endpoint not found"
                        else -> "Login failed (${response.code()}): ${response.message()}"
                    }
                    _loginState.value = NetworkResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                _loginState.value = NetworkResult.Error("Network error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(username: String, email: String, password: String, firstName: String = "", lastName: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _registerState.value = NetworkResult.Loading
            
            try {
                val response = jwtAuthApiService.registerUser(
                    JWTRegisterRequest(username, email, password, firstName, lastName)
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val registerResponse = response.body()!!
                    
                    // After successful registration, automatically login
                    login(username, password)
                    _registerState.value = NetworkResult.Success(registerResponse)
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Registration failed. Please check your details."
                        409 -> "Username or email already exists."
                        404 -> "Registration endpoint not found"
                        else -> "Registration failed (${response.code()}): ${response.message()}"
                    }
                    _registerState.value = NetworkResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                _registerState.value = NetworkResult.Error("Network error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = NetworkResult.Loading
    }

    fun resetRegisterState() {
        _registerState.value = NetworkResult.Loading
    }
}
```

### **Step 5: Update Login/Register Screens**

Update your login screen to handle the new response models. The UI code can remain mostly the same, but update the state handling:

```kotlin
// In LoginScreen.kt, update the state handling:
val loginState by authViewModel.loginState.collectAsState()

LaunchedEffect(loginState) {
    when (loginState) {
        is NetworkResult.Success -> {
            // Login successful
            onLoginSuccess()
        }
        is NetworkResult.Error -> {
            // Show error message
            val errorMessage = (loginState as NetworkResult.Error).message
            // Display error to user
        }
        else -> {
            // Loading or other states
        }
    }
}
```

### **Step 6: Update Dependency Injection**

Update your `RepositoryModule.kt` to include the new JWT auth service:

```kotlin
@Provides
@Singleton
fun provideUserRepository(
    jwtAuthApiService: JWTAuthApiService,
    tokenManager: TokenManager
): UserRepository {
    return UserRepository(jwtAuthApiService, tokenManager)
}
```


