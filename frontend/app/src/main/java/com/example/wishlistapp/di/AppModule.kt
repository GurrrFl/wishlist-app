package com.example.wishlistapp.di

import android.content.SharedPreferences
import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.remote.UserApi
import com.example.wishlistapp.data.repository.AuthRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// http://10.0.2.2:8000/
val appModule = module {
    single<SharedPreferences> {
      androidContext().getSharedPreferences("session_prefs", android.content.Context.MODE_PRIVATE)
    }

    single {
        Retrofit.Builder()
            .baseUrl("http://wishlist-app-3owv.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<UserApi> {
        get<Retrofit>().create(UserApi::class.java)
    }

    single { SessionManager(get()) }

    single { AuthRepository(get(), get()) }


}
