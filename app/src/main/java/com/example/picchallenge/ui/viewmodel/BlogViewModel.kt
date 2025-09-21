package com.example.picchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picchallenge.data.model.BlogPostDisplay
import com.example.picchallenge.data.repository.BlogRepository
import com.example.picchallenge.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlogViewModel @Inject constructor(
    private val blogRepository: BlogRepository
) : ViewModel() {
    
    private val _blogPosts = MutableStateFlow<NetworkResult<List<BlogPostDisplay>>>(NetworkResult.Loading)
    val blogPosts: StateFlow<NetworkResult<List<BlogPostDisplay>>> = _blogPosts.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadNewsPosts()
    }
    
    fun loadNewsPosts(page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            _blogPosts.value = NetworkResult.Loading
            
            when (val result = blogRepository.getNewsPosts(page, perPage)) {
                is NetworkResult.Success -> {
                    _blogPosts.value = NetworkResult.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _blogPosts.value = NetworkResult.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _blogPosts.value = NetworkResult.Loading
                }
            }
            _isLoading.value = false
        }
    }
    
    fun loadAllPosts(page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            _blogPosts.value = NetworkResult.Loading
            
            when (val result = blogRepository.getAllPosts(page, perPage)) {
                is NetworkResult.Success -> {
                    _blogPosts.value = NetworkResult.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _blogPosts.value = NetworkResult.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _blogPosts.value = NetworkResult.Loading
                }
            }
            _isLoading.value = false
        }
    }
    
    fun refreshPosts() {
        loadNewsPosts()
    }
}
