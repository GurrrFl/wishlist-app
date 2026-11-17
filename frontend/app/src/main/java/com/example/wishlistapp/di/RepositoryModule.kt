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
import com.example.wishlistapp.domain.action.AddGiftAction
import com.example.wishlistapp.domain.action.CancelReservationAction
import com.example.wishlistapp.domain.action.CreateWishlistAction
import com.example.wishlistapp.domain.action.ReserveGiftAction
import com.example.wishlistapp.data.local.FakeSnapshotStorage
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { AuthRepository(get(), get()) }
    single { FakeSnapshotStorage(androidContext().getSharedPreferences("demo_data", Context.MODE_PRIVATE)) }
    single { FakeDataStore(get()) }
    single { NetworkWishlistRepository(get(), get(), get()) }
    single<WishlistRepository> {
        if (BuildConfig.NETWORK_DATA_SOURCE) get<NetworkWishlistRepository>()
        else FakeWishlistRepository(get(), get())
    }
    single { FakeGiftRepository(get(), get()) }
    single<GiftRepository> {
        if (BuildConfig.NETWORK_DATA_SOURCE) NetworkGiftRepository(get())
        else get<FakeGiftRepository>()
    }
    single<ReservationRepository> {
        if (BuildConfig.NETWORK_DATA_SOURCE) NetworkReservationRepository(get(), get())
        else FakeReservationRepository(get(), get())
    }
    factory { CreateWishlistAction(get()) }
    factory { AddGiftAction(get()) }
    factory { ReserveGiftAction(get()) }
    factory { CancelReservationAction(get()) }
}
