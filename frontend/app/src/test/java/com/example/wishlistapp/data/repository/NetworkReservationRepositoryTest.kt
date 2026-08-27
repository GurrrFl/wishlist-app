package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.inMemoryPreferences
import com.example.wishlistapp.data.model.dto.ReservationDto
import com.example.wishlistapp.data.remote.ReservationApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkReservationRepositoryTest {
    @Test fun cacheDoesNotDuplicateReservationAndClearsOnCancel() = runBlocking {
        val dto = ReservationDto(8, 10, 2, "2026-09-03T09:46:00", null)
        val api = object : ReservationApi {
            override suspend fun reserve(giftId: Int) = dto
            override suspend fun getMine(onlyActive: Boolean) = listOf(dto)
            override suspend fun cancel(id: Int) = dto.copy(cancelledAt = "2026-09-03T10:00:00")
        }
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "Guest") }
        val repository = NetworkReservationRepository(api, session)

        repository.reserveGift(10)
        repository.reserveGift(10)
        assertEquals(1, repository.observeMyReservations().first().size)
        repository.cancelReservation(8)
        assertEquals(emptyList<Any>(), repository.observeMyReservations().first())
    }
}
