package com.example.picchallenge.di

import com.example.picchallenge.data.remote.AuthApiService
import com.example.picchallenge.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthNetworkModule {

    @Provides
    @Singleton
    fun provideAuthApiService(
        settingsRepository: SettingsRepository
    ): AuthApiService {
        val baseUrl = runBlocking {
            settingsRepository.jwtBaseUrl.first()
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build()) // Simple client for auth
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}
