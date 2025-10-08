package com.example.picchallenge.di

import android.content.Context
import com.example.picchallenge.data.remote.PhotoContestApiService
import com.example.picchallenge.data.remote.WordPressApiService
import com.example.picchallenge.data.repository.BlogRepository
import com.example.picchallenge.data.repository.ContestRepository
import com.example.picchallenge.data.repository.PhotoRepository
import com.example.picchallenge.data.repository.SettingsRepository
import com.example.picchallenge.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideContestRepository(
        apiService: PhotoContestApiService
    ): ContestRepository {
        return ContestRepository(apiService)
    }

    @Provides
    @Singleton
    fun providePhotoRepository(
        apiService: PhotoContestApiService
    ): PhotoRepository {
        return PhotoRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: PhotoContestApiService
    ): UserRepository {
        return UserRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideBlogRepository(
        wordPressApiService: WordPressApiService
    ): BlogRepository {
        return BlogRepository(wordPressApiService)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}
