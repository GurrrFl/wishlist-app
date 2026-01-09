package com.example.wishlistapp.di

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.remote.UserApi
import com.example.wishlistapp.data.repository.AuthRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    single {
        Retrofit.Builder()
            .baseUrl("https://sllhxm-181-214-131-107.ru.tuna.am/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<UserApi> {
        get<Retrofit>().create(UserApi::class.java)
    }

    single { SessionManager(androidContext()) }

    single { AuthRepository(get(), get()) }

}
