package com.example.vkunsubscribe

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.drawee.backends.pipeline.Fresco
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Fresco.initialize(this)
        VK.addTokenExpiredHandler(tokenTracker)
    }

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            // token expired
        }
    }
}