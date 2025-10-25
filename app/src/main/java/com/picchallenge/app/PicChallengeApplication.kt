package com.picchallenge.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PicChallengeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization code can go here
    }
}
