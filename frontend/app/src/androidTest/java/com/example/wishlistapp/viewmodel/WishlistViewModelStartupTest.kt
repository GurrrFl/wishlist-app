package com.example.wishlistapp.viewmodel

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.repository.FakeDataStore
import com.example.wishlistapp.data.repository.FakeGiftRepository
import com.example.wishlistapp.data.repository.FakeReservationRepository
import com.example.wishlistapp.data.repository.FakeWishlistRepository
import com.example.wishlistapp.domain.action.AddGiftAction
import com.example.wishlistapp.domain.action.CancelReservationAction
import com.example.wishlistapp.domain.action.CreateWishlistAction
import com.example.wishlistapp.domain.action.ReserveGiftAction
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WishlistViewModelStartupTest {
    @Test
    fun startupLoadsAfterStateIsInitialized() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val preferences = instrumentation.targetContext.getSharedPreferences(
                "wishlist_viewmodel_startup_test",
                Context.MODE_PRIVATE,
            )
            val session = SessionManager(preferences)
            val store = FakeDataStore()
            val wishlists = FakeWishlistRepository(store, session)
            val gifts = FakeGiftRepository(store)
            val reservations = FakeReservationRepository(store, gifts, session)

            val viewModel = WishlistViewModel(
                wishlists,
                gifts,
                reservations,
                session,
                CreateWishlistAction(wishlists),
                AddGiftAction(gifts),
                ReserveGiftAction(reservations),
                CancelReservationAction(reservations),
            )

            assertTrue(viewModel.wishlistsState is LoadState.Content)
        }
    }
}
