package com.example.picchallenge.di

import com.example.picchallenge.data.remote.PhotoContestApiService
import com.example.picchallenge.data.remote.VotingApiService
import com.example.picchallenge.data.remote.WordPressApiService
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
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DynamicNetworkModule {

    @Provides
    @Singleton
    @PhotoContestRetrofit
    fun provideDynamicRetrofit(
        okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository
    ): Retrofit {
        val baseUrl = runBlocking {
            val mainUrl = settingsRepository.baseUrl.first()
            println("DEBUG: Original base URL from settings: $mainUrl")
            // Construct the correct photo contest API base URL
            val cleanUrl = mainUrl.removeSuffix("/").removeSuffix("wp-json").removeSuffix("/")
            val finalUrl = "$cleanUrl/wp-json/photo-contest/v1/"
            println("DEBUG: Final photo contest API URL: $finalUrl")
            finalUrl
        }
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDynamicPhotoContestApiService(
        @Singleton okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository
    ): PhotoContestApiService {
        val retrofit = provideDynamicRetrofit(okHttpClient, settingsRepository)
        return retrofit.create(PhotoContestApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideVotingApiService(
        okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository
    ): VotingApiService {
        val baseUrl = runBlocking {
            val mainUrl = settingsRepository.baseUrl.first()
            // Use the main WordPress API base URL for voting endpoints
            val cleanUrl = mainUrl.removeSuffix("/").removeSuffix("wp-json").removeSuffix("/")
            "$cleanUrl/wp-json/"
        }
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        return retrofit.create(VotingApiService::class.java)
    }

}

// Alternative approach: Create a factory that can update the base URL
@Singleton
class RetrofitFactory @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
