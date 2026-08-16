package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime

class FakeReservationRepository(private val store: FakeDataStore, private val gifts: FakeGiftRepository, private val sessionManager: SessionManager) : ReservationRepository {
    override fun observeMyReservations(): Flow<List<Reservation>> = combine(store.reservations, sessionManager.userId) { reservations, userId ->
        reservations.filter { it.userId == userId }
    }
    override suspend fun reserveGift(giftId: Int): AppResult<Reservation> {
        val userId = sessionManager.getUserId() ?: return AppResult.Failure(AppError.Unauthorized)
        val gift = store.wishlists.value.flatMap { it.gifts }.find { it.id == giftId }
            ?: return AppResult.Failure(AppError.NotFound)
        val wishlist = store.wishlists.value.find { it.id == gift.wishlistId } ?: return AppResult.Failure(AppError.NotFound)
        if (wishlist.ownerId == userId || wishlist.isPrivate) return AppResult.Failure(AppError.Forbidden)
        if (gift.status == GiftStatus.RESERVED) return AppResult.Failure(AppError.Conflict)
        val reservation = Reservation(store.nextReservationId++, giftId, userId, currentUserName, LocalDateTime.now())
        gifts.change(giftId) { it.copy(status = GiftStatus.RESERVED, reservedBy = currentUserName) }
        store.updateReservations { it + reservation }
        return AppResult.Success(reservation)
    }
    override suspend fun cancelReservation(reservationId: Int): AppResult<Reservation> {
        val reservation = store.reservations.value.find { it.id == reservationId && it.userId == currentUserId } ?: return AppResult.Failure(AppError.NotFound)
        gifts.change(reservation.giftId) { it.copy(status = GiftStatus.AVAILABLE, reservedBy = null) }
        store.updateReservations { it.filterNot { item -> item.id == reservationId } }
        return AppResult.Success(reservation)
    }
    override suspend fun getMyReservations() = AppResult.Success(store.reservations.value.filter { it.userId == currentUserId })
    private val currentUserId get() = sessionManager.getUserId() ?: 0
    private val currentUserName get() = sessionManager.getUserName() ?: "User"
}
