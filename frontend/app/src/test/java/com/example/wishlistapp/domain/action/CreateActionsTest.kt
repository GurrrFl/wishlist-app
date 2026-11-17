package com.example.wishlistapp.domain.action

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.inMemoryPreferences
import com.example.wishlistapp.data.repository.FakeDataStore
import com.example.wishlistapp.data.repository.FakeGiftRepository
import com.example.wishlistapp.data.repository.FakeWishlistRepository
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CreateActionsTest {
    @Test fun wishlistActionValidatesDateAndUsesRepository() = runBlocking {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "Guest") }
        val action = CreateWishlistAction(FakeWishlistRepository(FakeDataStore(), session))

        assertTrue((action("Birthday", false, "invalid") as AppResult.Failure).error is AppError.Validation)
        val created = action("Birthday", false, "01.08.2026") as AppResult.Success
        assertEquals(LocalDate.of(2026, 8, 1), created.data.eventDate)
        assertEquals(2, created.data.ownerId)
    }

    @Test fun giftActionRejectsInvalidPriceAndParsesDecimalComma() = runBlocking {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "Guest") }
        val action = AddGiftAction(FakeGiftRepository(FakeDataStore(), session))

        assertTrue((action(4, "Book", "bad", "", "") as AppResult.Failure).error is AppError.Validation)
        val created = action(4, "Book", "12,50", "", "") as AppResult.Success
        assertEquals(12.5, created.data.price!!, 0.0)
    }
}
