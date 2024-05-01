package com.example.musicapp.app

import android.app.Application
import com.permissionx.guolindev.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.BUILD_TYPE == "debug") {
            Timber.plant(Timber.DebugTree())
        }
    }
}