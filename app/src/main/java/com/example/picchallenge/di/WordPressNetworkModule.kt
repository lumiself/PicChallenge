package com.example.picchallenge.di

import com.example.picchallenge.data.remote.WordPressApiService
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
object WordPressNetworkModule {

    @Provides
    @Singleton
    @WordPressRetrofit
    fun provideWordPressRetrofit(
        okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository
    ): Retrofit {
        val baseUrl = runBlocking {
            val mainUrl = settingsRepository.baseUrl.first()
            // Construct the correct WordPress API base URL
            val cleanUrl = mainUrl.removeSuffix("/").removeSuffix("wp-json").removeSuffix("/")
            "$cleanUrl/wp-json/wp/v2/"
        }
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWordPressApiService(
        @WordPressRetrofit retrofit: Retrofit
    ): WordPressApiService {
        return retrofit.create(WordPressApiService::class.java)
    }
}
