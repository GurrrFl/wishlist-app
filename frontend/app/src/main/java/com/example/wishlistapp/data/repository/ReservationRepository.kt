package com.example.wishlistapp.data.repository

import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    fun observeMyReservations(): Flow<List<Reservation>>
    suspend fun reserveGift(giftId: Int): AppResult<Reservation>
    suspend fun cancelReservation(reservationId: Int): AppResult<Reservation>
    suspend fun getMyReservations(): AppResult<List<Reservation>>
}
