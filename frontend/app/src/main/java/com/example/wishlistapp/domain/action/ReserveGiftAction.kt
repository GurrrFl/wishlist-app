package com.example.wishlistapp.domain.action

import com.example.wishlistapp.data.repository.ReservationRepository
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult

class ReserveGiftAction(private val repository: ReservationRepository) {
    suspend operator fun invoke(giftId: Int): AppResult<Reservation> {
        if (giftId <= 0) return AppResult.Failure(AppError.Validation("Некорректный подарок"))
        return repository.reserveGift(giftId)
    }
}
