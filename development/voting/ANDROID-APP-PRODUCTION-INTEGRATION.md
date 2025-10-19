# 📱 Android App Production Integration Guide
## Photo Contest Voting System with JWT Authentication
**Production URL**: `https://lumiself.co.zw/modeling`

---

## 🚀 Quick Start

### Base URLs
```kotlin
// Production API Endpoints
const val BASE_URL = "https://lumiself.co.zw/modeling"
const val PHOTO_CONTEST_API = "$BASE_URL/wp-json/photo-contest/v1/"
const val JWT_USER_API = "$BASE_URL/wp-json/jwt-um/v1/"
```

---

## 📋 API Endpoints Overview

### 🔓 Public Endpoints (No Authentication Required)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/wp-json/photo-contest/v1/contests` | GET | Get all contests |
| `/wp-json/photo-contest/v1/contests/{id}/photos` | GET | Get contest photos |
| `/wp-json/photo-contest/v1/photos/popular` | GET | Get popular photos |
| `/wp-json/photo-contest/v1/photos/recent` | GET | Get recent photos |
| `/wp-json/jwt-um/v1/register` | POST | Register new user |
| `/wp-json/jwt-um/v1/login` | POST | User login |

### 🔒 Authenticated Endpoints (JWT Token Required)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/wp-json/photo-contest/v1/photos/{id}/vote` | POST | Vote for photo |
| `/wp-json/jwt-um/v1/user/profile` | GET | Get user profile |
| `/wp-json/jwt-um/v1/user/can-vote` | GET | Check voting eligibility |
| `/wp-json/jwt-um/v1/user/voting-history` | GET | Get voting history |

---

## 🏗️ Complete Android Implementation

### 1. Data Models

```kotlin
// User Models
data class UserRegistration(
    val username: String,
    val email: String,
    val password: String,
    val first_name: String = "",
    val last_name: String = ""
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String,
    val user: User,
    val message: String
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val display_name: String,
    val avatar: String
)

// Contest Models
data class Contest(
    val id: Int,
    val name: String,
    val start_date: String,
    val end_date: String,
    val vote_start_date: String,
    val register_end_date: String,
    val description: String,
    val image_per_user: Int,
    val vote_frequency: Int,
    val gallery_layout: Int,
    val contest_mode: Int,
    val status: String
)

data class Photo(
    val id: Int,
    val title: String,
    val description: String,
    val url: String,
    val thumbnail: String,
    val medium: String,
    val large: String,
    val votes: Int,
    val views: Int,
    val author: String,
    val author_id: Int,
    val date: String,
    val contest_id: Int,
    val category_id: Int
)

// Voting Models
data class VoteRequest(
    val email: String
)

data class VoteResponse(
    val success: Boolean,
    val message: String,
    val new_vote_count: Int
)

data class VotingEligibility(
    val can_vote: Boolean,
    val reason: String?,
    val message: String
)
```

### 2. Retrofit API Interface

```kotlin
import retrofit2.Response
import retrofit2.http.*

interface PhotoContestApi {
    // Public endpoints
    @GET("contests")
    suspend fun getContests(): ContestResponse
    
    @GET("contests/{id}/photos")
    suspend fun getContestPhotos(
        @Path("id") contestId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): PhotosResponse
    
    @GET("photos/popular")
    suspend fun getPopularPhotos(
        @Query("limit") limit: Int = 20
    ): List<Photo>
    
    @GET("photos/recent")
    suspend fun getRecentPhotos(
        @Query("limit") limit: Int = 20
    ): List<Photo>
    
    // Authenticated endpoints
    @POST("photos/{id}/vote")
    suspend fun voteForPhoto(
        @Header("Authorization") token: String,
        @Path("id") photoId: Int,
        @Body voteRequest: VoteRequest
    ): VoteResponse
}

interface JWTUserApi {
    // Public endpoints
    @POST("register")
    suspend fun registerUser(@Body registration: UserRegistration): RegistrationResponse
    
    @POST("login")
    suspend fun loginUser(@Body login: LoginRequest): LoginResponse
    
    // Authenticated endpoints
    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): UserProfileResponse
    
    @GET("user/can-vote")
    suspend fun canUserVote(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int
    ): VotingEligibility
    
    @GET("user/voting-history")
    suspend fun getVotingHistory(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int? = null,
        @Query("page") page: Int = 1
    ): VotingHistoryResponse
}

// Response wrapper classes
data class ContestResponse(
    val data: List<Contest>,
    val total: Int,
    val page: Int,
    val per_page: Int,
    val pages: Int
)

data class PhotosResponse(
    val data: List<Photo>,
    val total: Int,
    val page: Int,
    val per_page: Int,
    val pages: Int
)

data class RegistrationResponse(
    val success: Boolean,
    val user_id: Int,
    val username: String,
    val email: String,
    val message: String,
    val verification_required: Boolean
)

data class UserProfileResponse(
    val success: Boolean,
    val user: User
)

data class VotingHistoryResponse(
    val data: List<VoteHistory>,
    val total: Int,
    val page: Int,
    val per_page: Int,
    val pages: Int
)

data class VoteHistory(
    val id: Int,
    val photo_id: Int,
    val photo_title: String,
    val contest_id: Int,
    val contest_name: String,
    val vote_date: String
)
```

### 3. Retrofit Configuration

```kotlin
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://lumiself.co.zw/modeling/wp-json/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val photoContestApi: PhotoContestApi by lazy {
        retrofit.create(PhotoContestApi::class.java)
    }
    
    val jwtUserApi: JWTUserApi by lazy {
        retrofit.create(JWTUserApi::class.java)
    }
}
```

### 4. Token Manager

```kotlin
import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }
    
    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }
    
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }
    
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }
    
    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }
    
    fun saveUserData(user: User) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_EMAIL, user.email)
            apply()
        }
    }
    
    fun getUserData(): User? {
        val userId = prefs.getInt(KEY_USER_ID, -1)
        if (userId == -1) return null
        
        return User(
            id = userId,
            username = prefs.getString(KEY_USERNAME, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            first_name = "",
            last_name = "",
            display_name = "",
            avatar = ""
        )
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
```

### 5. Repository Pattern

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoContestRepository @Inject constructor(
    private val photoContestApi: PhotoContestApi,
    private val jwtUserApi: JWTUserApi,
    private val tokenManager: TokenManager
) {
    
    // User Authentication
    suspend fun registerUser(userData: UserRegistration): Result<RegistrationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = jwtUserApi.registerUser(userData)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun loginUser(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = jwtUserApi.loginUser(LoginRequest(username, password))
                if (response.success) {
                    tokenManager.saveToken(response.token)
                    tokenManager.saveUserData(response.user)
                }
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken() ?: throw Exception("No token found")
                val response = jwtUserApi.getUserProfile("Bearer $token")
                if (response.success) {
                    Result.success(response.user)
                } else {
                    Result.failure(Exception("Failed to get user profile"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Contests
    suspend fun getContests(): Result<List<Contest>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoContestApi.getContests()
                Result.success(response.data)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getContestPhotos(contestId: Int, page: Int = 1): Result<List<Photo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoContestApi.getContestPhotos(contestId, page)
                Result.success(response.data)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Voting
    suspend fun canUserVote(contestId: Int): Result<VotingEligibility> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken() ?: throw Exception("Not logged in")
                val response = jwtUserApi.canUserVote("Bearer $token", contestId)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun voteForPhoto(photoId: Int, contestId: Int): Result<VoteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken() ?: throw Exception("Not logged in")
                val user = tokenManager.getUserData() ?: throw Exception("User data not found")
                
                // Check eligibility first
                val eligibility = canUserVote(contestId).getOrThrow()
                if (!eligibility.can_vote) {
                    throw Exception(eligibility.message)
                }
                
                // Proceed with voting
                val response = photoContestApi.voteForPhoto(
                    "Bearer $token",
                    photoId,
                    VoteRequest(user.email)
                )
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getVotingHistory(contestId: Int? = null): Result<List<VoteHistory>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken() ?: throw Exception("Not logged in")
                val response = jwtUserApi.getVotingHistory("Bearer $token", contestId)
                Result.success(response.data)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    fun logout() {
        tokenManager.clearAll()
    }
}
```

---

## 🎯 Complete User Flow Implementation

### 1. Registration Screen (XML)

```xml
<!-- res/layout/activity_register.xml -->
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <TextView
            android:text="Join Photo Contest"
            android:textSize="24sp"
            android:textStyle="bold"
            android:layout_marginBottom="24dp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp">
            
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/usernameInput"
                android:hint="Username"
                android:inputType="text"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp">
            
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/emailInput"
                android:hint="Email"
                android:inputType="textEmailAddress"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp">
            
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/passwordInput"
                android:hint="Password"
                android:inputType="textPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp">
            
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/firstNameInput"
                android:hint="First Name"
                android:inputType="textPersonName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="24dp">
            
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/lastNameInput"
                android:hint="Last Name"
                android:inputType="textPersonName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/registerButton"
            android:text="Register"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp" />

        <TextView
            android:id="@+id/loginLink"
            android:text="Already have an account? Login"
            android:textColor="@color/primary"
            android:layout_gravity="center"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
    </LinearLayout>
</ScrollView>
```

### 2. Registration Activity (Kotlin)

```kotlin
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    
    private val viewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        setupViews()
        observeViewModel()
    }
    
    private fun setupViews() {
        registerButton.setOnClickListener {
            performRegistration()
        }
        
        loginLink.setOnClickListener {
            finish() // Go back to login
        }
    }
    
    private fun performRegistration() {
        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        
        // Validate input
        if (validateInput(username, email, password)) {
            val userData = UserRegistration(
                username = username,
                email = email,
                password = password,
                first_name = firstName,
                last_name = lastName
            )
            
            showLoading(true)
            lifecycleScope.launch {
                viewModel.registerUser(userData)
            }
        }
    }
    
    private fun validateInput(username: String, email: String, password: String): Boolean {
        when {
            username.isEmpty() -> {
                usernameInput.error = "Username is required"
                return false
            }
            username.length < 3 -> {
                usernameInput.error = "Username must be at least 3 characters"
                return false
            }
            email.isEmpty() -> {
                emailInput.error = "Email is required"
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInput.error = "Invalid email format"
                return false
            }
            password.isEmpty() -> {
                passwordInput.error = "Password is required"
                return false
            }
            password.length < 8 -> {
                passwordInput.error = "Password must be at least 8 characters"
                return false
            }
            else -> return true
        }
    }
    
    private fun observeViewModel() {
        viewModel.registrationState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading(true)
                is AuthState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                    finish()
                }
                is AuthState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        registerButton.isEnabled = !show
        registerButton.text = if (show) "Registering..." else "Register"
    }
}
```

### 3. Contest List with Voting Check

```kotlin
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContestAdapter(
    private val contests: List<Contest>,
    private val onContestClick: (Contest) -> Unit,
    private val onLoginRequired: () -> Unit
) : RecyclerView.Adapter<ContestAdapter.ContestViewHolder>() {

    inner class ContestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.contestName)
        private val dateText: TextView = itemView.findViewById(R.id.contestDate)
        private val statusText: TextView = itemView.findViewById(R.id.contestStatus)
        private val viewButton: Button = itemView.findViewById(R.id.viewButton)

        fun bind(contest: Contest) {
            nameText.text = contest.name
            dateText.text = "Ends: ${contest.end_date}"
            statusText.text = contest.status.capitalize()
            
            viewButton.setOnClickListener {
                onContestClick(contest)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contest, parent, false)
        return ContestViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContestViewHolder, position: Int) {
        holder.bind(contests[position])
    }

    override fun getItemCount() = contests.size
}
```

### 4. Complete Voting Flow

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContestViewModel @Inject constructor(
    private val repository: PhotoContestRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _contests = MutableStateFlow<List<Contest>>(emptyList())
    val contests: StateFlow<List<Contest>> = _contests.asStateFlow()

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _votingState = MutableStateFlow<VotingState>(VotingState.Idle)
    val votingState: StateFlow<VotingState> = _votingState.asStateFlow()

    fun loadContests() {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repository.getContests()) {
                is Result.Success -> {
                    _contests.value = result.getOrNull() ?: emptyList()
                    _error.value = null
                }
                is Result.Failure -> {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load contests"
                }
            }
            _loading.value = false
        }
    }

    fun loadContestPhotos(contestId: Int) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repository.getContestPhotos(contestId)) {
                is Result.Success -> {
                    _photos.value = result.getOrNull() ?: emptyList()
                    _error.value = null
                }
                is Result.Failure -> {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load photos"
                }
            }
            _loading.value = false
        }
    }

    fun checkVotingEligibility(contestId: Int, onResult: (VotingEligibility) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.canUserVote(contestId)) {
                is Result.Success -> {
                    onResult(result.getOrNull() ?: VotingEligibility(false, "error", "Unknown error"))
                }
                is Result.Failure -> {
                    onResult(VotingEligibility(false, "error", result.exceptionOrNull()?.message ?: "Failed to check eligibility"))
                }
            }
        }
    }

    fun voteForPhoto(photoId: Int, contestId: Int) {
        viewModelScope.launch {
            _votingState.value = VotingState.Loading
            when (val result = repository.voteForPhoto(photoId, contestId)) {
                is Result.Success -> {
                    _votingState.value = VotingState.Success(result.getOrNull()?.message ?: "Vote submitted successfully!")
                    // Reload photos to update vote count
                    loadContestPhotos(contestId)
                }
                is Result.Failure -> {
                    _votingState.value = VotingState.Error(result.exceptionOrNull()?.message ?: "Failed to submit vote")
                }
            }
        }
    }

    fun logout() {
        repository.logout()
        _contests.value = emptyList()
        _photos.value = emptyList()
    }
}

sealed class VotingState {
    object Idle : VotingState()
    object Loading : VotingState()
    data class Success(val message: String) : VotingState()
    data class Error(val message: String) : VotingState()
}
```

---

## 🧪 Testing Your Integration

### 1. Test Registration
```kotlin
// In your test
@Test
fun testRegistration() = runTest {
    val userData = UserRegistration(
        username = "testuser",
        email = "test@example.com",
        password = "password123",
        first_name = "Test",
        last_name = "User"
    )
    
    val result = repository.registerUser(userData)
    assertTrue(result.isSuccess)
}
```

### 2. Test Login
```kotlin
@Test
fun testLogin() = runTest {
    val result = repository.loginUser("testuser", "password123")
    assertTrue(result.isSuccess)
    assertNotNull(result.getOrNull()?.token)
}
```

### 3. Test Voting Flow
```kotlin
@Test
fun testCompleteVotingFlow() = runTest {
    // Login first
    val loginResult = repository.loginUser("testuser", "password123")
    assertTrue(loginResult.isSuccess)
    
    // Check eligibility
    val eligibilityResult = repository.canUserVote(1)
    assertTrue(eligibilityResult.isSuccess)
    
    val eligibility = eligibilityResult.getOrNull()
    if (eligibility?.can_vote == true) {
        // Vote for photo
        val voteResult = repository.voteForPhoto(123, 1)
        assertTrue(voteResult.isSuccess)
    }
}
```

---

## 🛡️ Security Best Practices

### 1. Token Security
```kotlin
// Always use HTTPS in production
private const val BASE_URL = "https://lumiself.co.zw/modeling/wp-json/"

// Store tokens securely
class SecureTokenManager @Inject constructor(
    private val encryptedSharedPreferences: SharedPreferences
) {
    fun saveToken(token: String) {
        encryptedSharedPreferences.edit().putString("jwt_token", token).apply()
    }
}
```

### 2. Input Validation
```kotlin
private fun validateInput(username: String, email: String, password: String): Boolean {
    return when {
        username.isEmpty() -> false
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> false
        password.length < 8 -> false
        else -> true
    }
}
```

### 3. Error Handling
```kotlin
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Exception) : ApiResult<T>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: Exception) {
        ApiResult.Error(e)
    }
}
```

---

## 📊 Monitoring & Analytics

### Track User Engagement
```kotlin
// Track registration
analytics.logEvent("user_registered", mapOf(
    "username" to username,
    "method" to "email"
))

// Track voting
analytics.logEvent("vote_submitted", mapOf(
    "contest_id" to contestId,
    "photo_id" to photoId,
    "user_id" to userId
))

// Track errors
analytics.logEvent("api_error", mapOf(
    "endpoint" to endpoint,
    "error_code" to errorCode,
    "error_message" to errorMessage
))
```

---

## 🚨 Common Issues & Solutions

### 1. CORS Issues
```kotlin
// Add CORS headers to your WordPress .htaccess
Header set Access-Control-Allow-Origin "https://your-android-app-domain.com"
Header set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
Header set Access-Control-Allow-Headers "Content-Type, Authorization"
```

### 2. SSL Certificate Issues
```kotlin
// For development only - disable SSL verification
val okHttpClient = OkHttpClient.Builder()
    .hostnameVerifier { _, _ -> true }
    .build()
```

### 3. Token Expiration
```kotlin
// Handle token refresh
private suspend fun refreshTokenIfNeeded(): String {
    val currentToken = tokenManager.getToken()
    // Implement token refresh logic here
    return currentToken ?: throw Exception("Token refresh failed")
}
```

---

## 🎯 Production Checklist

- [ ] **HTTPS Enabled** - All API calls use HTTPS
- [ ] **Token Security** - Tokens stored securely in encrypted preferences
- [ ] **Error Handling** - Comprehensive error handling implemented
- [ ] **Input Validation** - All user inputs validated
- [ ] **Loading States** - Proper loading indicators shown
- [ ] **Offline Handling** - App handles network issues gracefully
- [ ] **Analytics** - User actions tracked for insights
- [ ] **Testing** - Unit and integration tests written
- [ ] **Documentation** - Code documented and maintainable
- [ ] **Performance** - API calls optimized for mobile

---

## 📞 Support

For issues with your production integration:

1. **Check WordPress Debug Logs** - Enable debugging in wp-config.php
2. **Monitor API Performance** - Use tools like New Relic or DataDog
3. **Test Endpoints** - Use Postman or the interactive test interface
4. **Check Server Logs** - Monitor Apache/Nginx error logs
5. **Verify SSL Certificate** - Ensure valid SSL certificate

**Production URL**: `https://lumiself.co.zw/modeling`

Your complete photo contest voting system is now ready for production Android app integration! 🚀
