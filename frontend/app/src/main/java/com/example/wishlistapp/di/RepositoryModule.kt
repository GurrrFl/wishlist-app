package com.example.wishlistapp.di

import com.example.wishlistapp.BuildConfig
import com.example.wishlistapp.data.repository.AuthRepository
import com.example.wishlistapp.data.repository.FakeDataStore
import com.example.wishlistapp.data.repository.FakeGiftRepository
import com.example.wishlistapp.data.repository.FakeReservationRepository
import com.example.wishlistapp.data.repository.FakeWishlistRepository
import com.example.wishlistapp.data.repository.GiftRepository
import com.example.wishlistapp.data.repository.NetworkGiftRepository
import com.example.wishlistapp.data.repository.NetworkReservationRepository
import com.example.wishlistapp.data.repository.NetworkWishlistRepository
import com.example.wishlistapp.data.repository.ReservationRepository
import com.example.wishlistapp.data.repository.WishlistRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AuthRepository(get(), get()) }
    single { FakeDataStore() }
    single<WishlistRepository> {
        if (BuildConfig.NETWORK_DATA_SOURCE) NetworkWishlistRepository(get(), get())
        else FakeWishlistRepository(get(), get())
    }
    single { FakeGiftRepository(get()) }
    single<GiftRepository> {
        if (BuildConfig.NETWORK_DATA_SOURCE) NetworkGiftRepository(get())
        else get<FakeGiftRepository>()
    }
    single<ReservationRepository> {
        if (BuildConfig.NETWORK_DATA_SOURCE) NetworkReservationRepository(get())
        else FakeReservationRepository(get(), get(), get())
    }
}
