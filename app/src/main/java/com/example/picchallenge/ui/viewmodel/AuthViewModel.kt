package com.example.picchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchallenge.data.model.LoginRequest
import com.example.picchallenge.data.model.LoginResponse
import com.example.picchallenge.data.model.RegisterRequest
import com.example.picchallenge.data.remote.WordPressApiService
import com.example.picchallenge.utils.NetworkResult
import com.example.picchallenge.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling user authentication
 * Manages login, registration, and token storage
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val wordPressApiService: WordPressApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginState = MutableStateFlow<NetworkResult<LoginResponse>>(NetworkResult.Loading)
    val loginState: StateFlow<NetworkResult<LoginResponse>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<NetworkResult<Unit>>(NetworkResult.Loading)
    val registerState: StateFlow<NetworkResult<Unit>> = _registerState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    /**
     * Check if user is already authenticated
     */
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

    /**
     * Login user with username and password
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = NetworkResult.Loading
            
            try {
                val response = wordPressApiService.loginUser(LoginRequest(username, password))
                
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    
                    // Save token securely
                    tokenManager.saveToken(
                        token = loginResponse.token,
                        userId = loginResponse.userId,
                        userEmail = loginResponse.userEmail,
                        userDisplayName = loginResponse.userDisplayName
                    )
                    
                    // Update auth state
                    _authState.value = AuthState.Authenticated(
                        userId = loginResponse.userId,
                        userEmail = loginResponse.userEmail,
                        userDisplayName = loginResponse.userDisplayName
                    )
                    
                    _loginState.value = NetworkResult.Success(loginResponse)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid username or password"
                        403 -> "Access denied. Please contact support."
                        else -> "Login failed. Please try again."
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

    /**
     * Register new user
     */
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _registerState.value = NetworkResult.Loading
            
            try {
                val response = wordPressApiService.registerUser(RegisterRequest(username, email, password))
                
                if (response.isSuccessful && response.body() != null) {
                    val registerResponse = response.body()!!
                    
                    // After successful registration, automatically login
                    login(username, password)
                    _registerState.value = NetworkResult.Success(Unit)
                } else {
                    // Try to get detailed error message from response
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrEmpty()) {
                        try {
                            // Try to parse error response
                            "Registration failed: $errorBody"
                        } catch (e: Exception) {
                            "Registration failed: ${response.message()}"
                        }
                    } else {
                        when (response.code()) {
                            400 -> "Registration failed. Please check your details."
                            409 -> "Username or email already exists."
                            404 -> "Registration endpoint not found"
                            500 -> "Server error. Please try again later."
                            else -> "Registration failed (${response.code()}): ${response.message()}"
                        }
                    }
                    _registerState.value = NetworkResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                _registerState.value = NetworkResult.Error("Network error: ${e.message ?: "Unknown error"}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reset login state
     */
    fun resetLoginState() {
        _loginState.value = NetworkResult.Loading
    }

    /**
     * Reset register state
     */
    fun resetRegisterState() {
        _registerState.value = NetworkResult.Loading
    }
}

/**
 * Authentication state sealed class
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(
        val userId: Int,
        val userEmail: String,
        val userDisplayName: String
    ) : AuthState()
}
