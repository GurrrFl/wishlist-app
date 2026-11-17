package com.example.wishlistapp.di

import android.content.Context
import android.content.SharedPreferences
import com.example.wishlistapp.BuildConfig
import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.remote.AuthInterceptor
import com.example.wishlistapp.data.remote.AuthApi
import com.example.wishlistapp.data.remote.WishlistApi
import com.example.wishlistapp.data.remote.GiftApi
import com.example.wishlistapp.data.remote.ReservationApi
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single<SharedPreferences> {
        androidContext().getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    }
    single { SessionManager(get(), allowDemo = !BuildConfig.NETWORK_DATA_SOURCE) }
    single { AuthInterceptor(get()) }
    single { OkHttpClient.Builder().addInterceptor(get<AuthInterceptor>()).build() }
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }
    single<WishlistApi> { get<Retrofit>().create(WishlistApi::class.java) }
    single<GiftApi> { get<Retrofit>().create(GiftApi::class.java) }
    single<ReservationApi> { get<Retrofit>().create(ReservationApi::class.java) }
}
