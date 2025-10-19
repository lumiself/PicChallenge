package com.example.picchallenge.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WordPressRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PhotoContestRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class JWTAuthRetrofit
