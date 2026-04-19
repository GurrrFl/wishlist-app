package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.mapper.toDomain
import com.example.wishlistapp.data.remote.ReservationApi
import com.example.wishlistapp.data.remote.remoteCall
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkReservationRepository(private val api: ReservationApi) : ReservationRepository {
    private val cache = MutableStateFlow<List<Reservation>>(emptyList())
    override fun observeMyReservations() = cache.asStateFlow()

    override suspend fun reserveGift(giftId: Int): AppResult<Reservation> {
        val result = remoteCall { api.reserve(giftId).toDomain() }
        if (result is AppResult.Success) cache.value = cache.value + result.data
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
