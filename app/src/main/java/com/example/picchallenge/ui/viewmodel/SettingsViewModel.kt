package com.example.picchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchallenge.data.repository.SettingsRepository
import com.example.picchallenge.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentUrl = MutableStateFlow("")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isValidUrl = MutableStateFlow(false)
    val isValidUrl: StateFlow<Boolean> = _isValidUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveResult = MutableStateFlow<NetworkResult<Boolean>?>(null)
    val saveResult: StateFlow<NetworkResult<Boolean>?> = _saveResult.asStateFlow()

    private val _isFirstRun = MutableStateFlow(true)
    val isFirstRun: StateFlow<Boolean> = _isFirstRun.asStateFlow()

    init {
        loadCurrentUrl()
    }

    private fun loadCurrentUrl() {
        viewModelScope.launch {
            val url = settingsRepository.baseUrl.first()
            _currentUrl.value = url
            _urlInput.value = url.removeSuffix("/")
            
            // Check if this is first run (default URL still set)
            _isFirstRun.value = url == "https://lumiself.co.zw/modeling/"
        }
    }

    fun updateUrlInput(url: String) {
        _urlInput.value = url
        _isValidUrl.value = settingsRepository.isValidUrl(url)
    }

    fun formatAndValidateUrl(url: String): String {
        return settingsRepository.formatUrl(url)
    }

    fun saveUrl() {
        viewModelScope.launch {
            _isLoading.value = true
            _saveResult.value = NetworkResult.Loading
            
            try {
                val formattedUrl = formatAndValidateUrl(_urlInput.value)
                settingsRepository.saveBaseUrl(formattedUrl)
                
                _currentUrl.value = formattedUrl
                _saveResult.value = NetworkResult.Success(true)
                _isFirstRun.value = false
                
            } catch (e: Exception) {
                _saveResult.value = NetworkResult.Error(e.message ?: "Failed to save URL")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun testConnection(): Boolean {
        // Basic validation - in a real app, you'd make an actual API call
        return _isValidUrl.value && _urlInput.value.isNotBlank()
    }

    fun dismissFirstRun() {
        _isFirstRun.value = false
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }
}
