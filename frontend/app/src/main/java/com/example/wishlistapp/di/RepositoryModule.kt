package com.example.wishlistapp.di

import com.example.wishlistapp.data.repository.AuthRepository
import com.example.wishlistapp.data.repository.FakeDataStore
import com.example.wishlistapp.data.repository.FakeGiftRepository
import com.example.wishlistapp.data.repository.FakeReservationRepository
import com.example.wishlistapp.data.repository.FakeWishlistRepository
import com.example.wishlistapp.data.repository.GiftRepository
import com.example.wishlistapp.data.repository.ReservationRepository
import com.example.wishlistapp.data.repository.WishlistRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AuthRepository(get(), get()) }
    single { FakeDataStore() }
    single<WishlistRepository> { FakeWishlistRepository(get(), get()) }
    single { FakeGiftRepository(get()) }
    single<GiftRepository> { get<FakeGiftRepository>() }
    single<ReservationRepository> { FakeReservationRepository(get(), get(), get()) }
}
