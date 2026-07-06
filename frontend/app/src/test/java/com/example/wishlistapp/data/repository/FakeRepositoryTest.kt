package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.inMemoryPreferences
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FakeRepositoryTest {
    @Test fun myWishlistsOnlyIncludeCurrentOwner() = runBlocking {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "kate_rosan") }
        val lists = FakeWishlistRepository(FakeDataStore(), session)

        val loaded = lists.getMyWishlists() as AppResult.Success
        assertTrue(loaded.data.isNotEmpty())
        assertTrue(loaded.data.all { it.ownerId == 2 })
        assertEquals(loaded.data, lists.observeMyWishlists().first())
    }

    @Test fun createGiftReserveAndCancelPreserveOwnershipRules() = runBlocking {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "kate_rosan") }
        val store = FakeDataStore()
        val lists = FakeWishlistRepository(store, session)
        val gifts = FakeGiftRepository(store)
        val reservations = FakeReservationRepository(store, gifts, session)

        val created = lists.createWishlist("New", LocalDate.of(2026, 6, 1), false) as AppResult.Success
        assertEquals(2, created.data.ownerId)
        val gift = gifts.createGift(created.data.id, "Book", 10.0, null, null) as AppResult.Success
        assertEquals(GiftStatus.AVAILABLE, gift.data.status)
        assertEquals(AppError.Forbidden, (reservations.reserveGift(gift.data.id) as AppResult.Failure).error)

        val reservation = reservations.reserveGift(1) as AppResult.Success
        assertEquals(AppError.Conflict, (reservations.reserveGift(1) as AppResult.Failure).error)
        assertTrue(reservations.getMyReservations() is AppResult.Success)
        reservations.cancelReservation(reservation.data.id)
        assertEquals(GiftStatus.AVAILABLE, (gifts.getGift(1) as AppResult.Success).data.status)
    }
}
