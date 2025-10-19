package com.example.picchallenge.di

import com.example.picchallenge.data.remote.JWTAuthApiService
import com.example.picchallenge.data.repository.SettingsRepository
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
