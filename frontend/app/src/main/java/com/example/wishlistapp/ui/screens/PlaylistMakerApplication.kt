package com.example.wishlistapp.ui.screens


import android.app.Application
import com.example.wishlistapp.di.initKoin

class PlaylistMakerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this)
    }
}
