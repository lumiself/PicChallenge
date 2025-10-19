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

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(
        val userId: Int,
        val userEmail: String,
        val userDisplayName: String
    ) : AuthState()
}
