package com.example.picchallenge.data.remote

import com.example.picchallenge.data.model.LoginRequest
import com.example.picchallenge.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("token")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
}
