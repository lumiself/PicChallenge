package com.example.picchallenge.data.remote

import com.example.picchallenge.data.model.WordPressPost
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WordPressApiService {
    
    @GET("posts")
    suspend fun getPosts(
        @Query("categories") categoryId: Int? = null,
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1,
        @Query("_embed") embed: String = "true" // This will include featured media and author info
    ): Response<List<WordPressPost>>
    
    @GET("posts")
    suspend fun getPostsByCategory(
        @Query("categories") categoryId: Int,
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1,
        @Query("_embed") embed: String = "true"
    ): Response<List<WordPressPost>>
}
