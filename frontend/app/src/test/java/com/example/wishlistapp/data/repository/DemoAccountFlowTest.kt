package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.demo.DemoAccount
import com.example.wishlistapp.data.inMemoryPreferences
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoAccountFlowTest {
    @Test fun eachDemoPersonaSeesOwnListsAndCanFindFriendsPublicList() = runBlocking {
        val session = SessionManager(inMemoryPreferences(), allowDemo = true)
        val store = FakeDataStore()
        val lists = FakeWishlistRepository(store, session)
        val gifts = FakeGiftRepository(store, session)
        val reservations = FakeReservationRepository(store, gifts, session)

        session.enterDemo(DemoAccount.ALEKSANDRA)
        assertEquals(setOf(1, 2, 3), lists.getMyWishlists().let { (it as AppResult.Success).data.map { list -> list.id }.toSet() })
        assertEquals(AppError.Forbidden, (reservations.reserveGift(1) as AppResult.Failure).error)
        assertEquals(4, (lists.findByPublicLink("56739") as AppResult.Success).data.id)

        session.enterDemo(DemoAccount.KATE)
        assertEquals(listOf(4), lists.observeMyWishlists().first().map { it.id })
        assertEquals(AppError.Forbidden, (lists.deleteWishlist(1) as AppResult.Failure).error)
        assertEquals(1, (lists.findByPublicLink("53291") as AppResult.Success).data.id)
        assertTrue(reservations.reserveGift(1) is AppResult.Success)
    }
}
