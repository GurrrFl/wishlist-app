package com.example.wishlistapp.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

fun initKoin(application: Application) {
    startKoin {
        androidContext(application)
        modules(
            appModule,
            presentationModule

        )
    }
}