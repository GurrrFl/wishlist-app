package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakeReservationRepository(private val store: FakeDataStore, private val gifts: FakeGiftRepository, private val sessionManager: SessionManager) : ReservationRepository {
    override fun observeMyReservations(): Flow<List<Reservation>> = store.reservations.map { reservations -> reservations.filter { it.reservedBy == currentUser } }
    override suspend fun reserveGift(giftId: Int): AppResult<Reservation> {
        val gift = (gifts.getGift(giftId) as? AppResult.Success)?.data ?: return AppResult.Failure(AppError.NotFound)
        val wishlist = store.wishlists.value.find { it.id == gift.wishlistId } ?: return AppResult.Failure(AppError.NotFound)
        if (wishlist.ownerName == currentUser) return AppResult.Failure(AppError.Forbidden)
        if (gift.status == GiftStatus.RESERVED) return AppResult.Failure(AppError.Conflict)
        val reservation = Reservation(store.nextReservationId++, giftId, currentUser)
        gifts.change(giftId) { it.copy(status = GiftStatus.RESERVED, reservedBy = currentUser) }
        store.updateReservations { it + reservation }
        return AppResult.Success(reservation)
    }
    override suspend fun cancelReservation(reservationId: Int): AppResult<Reservation> {
        val reservation = store.reservations.value.find { it.id == reservationId && it.reservedBy == currentUser } ?: return AppResult.Failure(AppError.NotFound)
        gifts.change(reservation.giftId) { it.copy(status = GiftStatus.AVAILABLE, reservedBy = null) }
        store.updateReservations { it.filterNot { item -> item.id == reservationId } }
        return AppResult.Success(reservation)
    }
    override suspend fun getMyReservations() = AppResult.Success(store.reservations.value.filter { it.reservedBy == currentUser })
    private val currentUser get() = sessionManager.getUserName() ?: "User"
}
