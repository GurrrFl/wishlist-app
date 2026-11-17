package com.example.wishlistapp.domain.action

import com.example.wishlistapp.data.repository.ReservationRepository
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult

class CancelReservationAction(private val repository: ReservationRepository) {
    suspend operator fun invoke(reservationId: Int): AppResult<Reservation> {
        if (reservationId <= 0) return AppResult.Failure(AppError.Validation("Некорректная бронь"))
        return repository.cancelReservation(reservationId)
    }
}
