package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.mapper.toDomain
import com.example.wishlistapp.data.remote.ReservationApi
import com.example.wishlistapp.data.remote.remoteCall
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class NetworkReservationRepository(private val api: ReservationApi, private val session: SessionManager) : ReservationRepository {
    private val cache = MutableStateFlow<List<Reservation>>(emptyList())
    override fun observeMyReservations() = combine(cache, session.userId) { reservations, userId ->
        reservations.filter { it.userId == userId }
    }

    override suspend fun reserveGift(giftId: Int): AppResult<Reservation> {
        val result = remoteCall { api.reserve(giftId).toDomain() }
        if (result is AppResult.Success) {
            cache.value = cache.value.filterNot { it.id == result.data.id } + result.data
        }
        return result
    }

    override suspend fun cancelReservation(reservationId: Int): AppResult<Reservation> {
        val result = remoteCall { api.cancel(reservationId).toDomain() }
        if (result is AppResult.Success) cache.value = cache.value.filterNot { it.id == reservationId }
        return result
    }

    override suspend fun getMyReservations(): AppResult<List<Reservation>> {
        val result = remoteCall { api.getMine().map { it.toDomain() } }
        if (result is AppResult.Success) cache.value = result.data
        return result
    }
}
