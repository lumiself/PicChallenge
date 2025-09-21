package com.example.picchallenge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PicChallengeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-wide components here
    }
}
