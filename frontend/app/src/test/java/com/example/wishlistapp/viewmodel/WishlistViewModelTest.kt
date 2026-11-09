package com.example.wishlistapp.viewmodel

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.inMemoryPreferences
import com.example.wishlistapp.data.repository.FakeDataStore
import com.example.wishlistapp.data.repository.FakeGiftRepository
import com.example.wishlistapp.data.repository.FakeReservationRepository
import com.example.wishlistapp.data.repository.FakeWishlistRepository
import com.example.wishlistapp.data.repository.GiftRepository
import com.example.wishlistapp.domain.action.AddGiftAction
import com.example.wishlistapp.domain.action.CancelReservationAction
import com.example.wishlistapp.domain.action.CreateWishlistAction
import com.example.wishlistapp.domain.action.ReserveGiftAction
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun newViewModel(userId: Int = 2): WishlistViewModel {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(userId, "Guest") }
        val store = FakeDataStore()
        val wishlists = FakeWishlistRepository(store, session)
        val gifts = FakeGiftRepository(store, session)
        val reservations = FakeReservationRepository(store, session)
        return WishlistViewModel(
            wishlists, gifts, reservations, session,
            CreateWishlistAction(wishlists), AddGiftAction(gifts),
            ReserveGiftAction(reservations), CancelReservationAction(reservations)
        )
    }

    @Test fun listAndDetailStatesAreIndependent() = runTest {
        val viewModel = newViewModel()
        runCurrent()
        assertTrue(viewModel.wishlistsState is LoadState.Content)

        viewModel.loadWishlist(4)
        assertTrue(viewModel.wishlistState == LoadState.Loading)
        assertTrue(viewModel.wishlistsState is LoadState.Content)
        runCurrent()

        assertTrue(viewModel.wishlistState is LoadState.Content)
        assertTrue(viewModel.wishlistsState is LoadState.Content)
    }

    @Test fun reservationAndCancellationRefreshPublicGift() = runTest {
        val viewModel = newViewModel()
        runCurrent()
        assertTrue(viewModel.findWishlist("53291") is AppResult.Success)
        runCurrent()

        val reservation = viewModel.reserveGiftAndWait(1) as AppResult.Success
        runCurrent()
        assertEquals(GiftStatus.RESERVED, viewModel.publicWishlist.value?.gifts?.first { it.id == 1 }?.status)
        assertEquals(reservation.data.id, viewModel.reservationIdForGift(1))

        assertTrue(viewModel.cancelReservationAndWait(reservation.data.id) is AppResult.Success)
        runCurrent()
        assertEquals(GiftStatus.AVAILABLE, viewModel.publicWishlist.value?.gifts?.first { it.id == 1 }?.status)
        assertEquals(null, viewModel.reservationIdForGift(1))
    }

    @Test fun repeatedMutationIsRejectedWithoutChangingListLoadingState() = runTest {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "Guest") }
        val store = FakeDataStore()
        val wishlists = FakeWishlistRepository(store, session)
        val fakeGifts = FakeGiftRepository(store, session)
        val gate = CompletableDeferred<Unit>()
        val gifts = object : GiftRepository by fakeGifts {
            override suspend fun createGift(wishlistId: Int, name: String, price: Double?, description: String?, link: String?): AppResult<Gift> {
                gate.await()
                return fakeGifts.createGift(wishlistId, name, price, description, link)
            }
        }
        val reservations = FakeReservationRepository(store, session)
        val viewModel = WishlistViewModel(
            wishlists, gifts, reservations, session, CreateWishlistAction(wishlists),
            AddGiftAction(gifts), ReserveGiftAction(reservations), CancelReservationAction(reservations)
        )
        runCurrent()
        val first = async { viewModel.addGiftAndWait("Book", "10", "", "", 4) }
        runCurrent()
        assertTrue(viewModel.isMutating)
        assertTrue(viewModel.wishlistsState is LoadState.Content)
        assertEquals(AppError.Conflict, (viewModel.addGiftAndWait("Second", "10", "", "", 4) as AppResult.Failure).error)
        gate.complete(Unit)
        assertTrue(first.await() is AppResult.Success)
        runCurrent()
        assertTrue(viewModel.mutationState is LoadState.Content)
        assertTrue(viewModel.wishlistsState is LoadState.Content)
    }
}
