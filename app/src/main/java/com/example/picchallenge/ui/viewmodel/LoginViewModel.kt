package com.example.picchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchallenge.data.repository.AuthRepository
import com.example.picchallenge.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    fun updateUsername(username: String) {
        _username.value = username
        validateInputs()
    }

    fun updatePassword(password: String) {
        _password.value = password
        validateInputs()
    }

    fun updateRememberMe(rememberMe: Boolean) {
        _rememberMe.value = rememberMe
    }

    fun login() {
        if (!isInputValid()) {
            _loginState.value = LoginState.Error("Please enter valid credentials")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            when (val result = authRepository.login(username.value, password.value)) {
                is NetworkResult.Success -> {
                    _loginState.value = LoginState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _loginState.value = LoginState.Error(result.message ?: "Login failed")
                }
                is NetworkResult.Loading -> {
                    _loginState.value = LoginState.Loading
                }
            }
        }
    }

    private fun validateInputs() {
        if (username.value.isNotBlank() && password.value.isNotBlank()) {
            _loginState.value = LoginState.Initial
        }
    }

    private fun isInputValid(): Boolean {
        return username.value.isNotBlank() && 
               password.value.length >= 4 && // Minimum password length
               username.value.length >= 3    // Minimum username length
    }

    fun resetState() {
        _loginState.value = LoginState.Initial
        _username.value = ""
        _password.value = ""
        _rememberMe.value = false
    }

    sealed class LoginState {
        object Initial : LoginState()
        object Loading : LoginState()
        data class Success(val loginResponse: com.example.picchallenge.data.model.LoginResponse) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
