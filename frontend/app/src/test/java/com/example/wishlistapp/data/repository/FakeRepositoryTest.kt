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

    @Test fun publicSearchPublishesOnlySharedWishlist() = runBlocking {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "kate_rosan") }
        val lists = FakeWishlistRepository(FakeDataStore(), session)

        val found = lists.findByPublicLink("https://wishlist.app/53291?source=share") as AppResult.Success
        assertEquals(1, found.data.id)
        assertEquals(found.data, lists.observePublicWishlist().first())
        assertEquals(AppError.NotFound, (lists.findByPublicLink("98342-private") as AppResult.Failure).error)
    }

    @Test fun wishlistMutationsRequireCurrentOwner() = runBlocking {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "kate_rosan") }
        val lists = FakeWishlistRepository(FakeDataStore(), session)

        assertEquals(AppError.Forbidden, (lists.getWishlist(1) as AppResult.Failure).error)
        assertEquals(AppError.Forbidden, (lists.updateWishlist(1, "Changed", null, null) as AppResult.Failure).error)
        assertEquals(AppError.Forbidden, (lists.deleteWishlist(1) as AppResult.Failure).error)
        assertEquals("День Рождения!", (lists.findByPublicLink("53291") as AppResult.Success).data.title)

        session.clear()
        assertEquals(AppError.Unauthorized, (lists.createWishlist("New", LocalDate.of(2026, 7, 1), false) as AppResult.Failure).error)
    }

    @Test fun giftMutationsRequireWishlistOwner() = runBlocking {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "kate_rosan") }
        val gifts = FakeGiftRepository(FakeDataStore(), session)

        assertEquals(AppError.Forbidden, (gifts.createGift(1, "Book", 10.0, null, null) as AppResult.Failure).error)
        assertEquals(AppError.Forbidden, (gifts.updateGift(1, "Changed", null, null, null) as AppResult.Failure).error)
        assertEquals(AppError.Forbidden, (gifts.deleteGift(1) as AppResult.Failure).error)
        assertEquals("AirPods или похожие", (gifts.getGift(1) as AppResult.Success).data.name)
    }

    @Test fun createGiftReserveAndCancelPreserveOwnershipRules() = runBlocking {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "kate_rosan") }
        val store = FakeDataStore()
        val lists = FakeWishlistRepository(store, session)
        val gifts = FakeGiftRepository(store, session)
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
