package com.example.wishlistapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.repository.GiftRepository
import com.example.wishlistapp.data.repository.ReservationRepository
import com.example.wishlistapp.data.repository.WishlistRepository
import com.example.wishlistapp.domain.action.AddGiftAction
import com.example.wishlistapp.domain.action.CancelReservationAction
import com.example.wishlistapp.domain.action.CreateWishlistAction
import com.example.wishlistapp.domain.action.ReserveGiftAction
import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed interface LoadState<out T> { data object Idle : LoadState<Nothing>; data object Loading : LoadState<Nothing>; data class Content<T>(val data: T) : LoadState<T>; data class Error(val message: String) : LoadState<Nothing> }
data class ReservedGift(val reservation: Reservation, val gift: Gift?)

class WishlistViewModel(private val wishlistRepository: WishlistRepository, private val giftRepository: GiftRepository, private val reservationRepository: ReservationRepository, private val sessionManager: SessionManager, private val createWishlistAction: CreateWishlistAction, private val addGiftAction: AddGiftAction, private val reserveGiftAction: ReserveGiftAction, private val cancelReservationAction: CancelReservationAction) : ViewModel() {
    val publicWishlist: StateFlow<Wishlist?> = wishlistRepository.observePublicWishlist()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val wishlists: StateFlow<List<Wishlist>> = wishlistRepository.observeMyWishlists().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val reservedGifts: StateFlow<List<ReservedGift>> = combine(wishlists, publicWishlist, reservationRepository.observeMyReservations()) { lists, public, reservations ->
        val giftsById = (lists.flatMap { it.gifts } + public?.gifts.orEmpty()).associateBy { it.id }
        reservations.map { reservation -> ReservedGift(reservation, giftsById[reservation.giftId]) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    var wishlistsState by mutableStateOf<LoadState<List<Wishlist>>>(LoadState.Idle); private set
    var wishlistState by mutableStateOf<LoadState<Wishlist>>(LoadState.Idle); private set

    init {
        loadMyWishlists()
        viewModelScope.launch { reservationRepository.getMyReservations() }
    }

    fun isWishlistOwner(wishlistId: Int): Boolean {
        val wishlist = getWishlist(wishlistId)
            ?: publicWishlist.value?.takeIf { it.id == wishlistId }
        return wishlist != null && wishlist.ownerId == sessionManager.getUserId()
    }
    fun reservationIdForGift(giftId: Int) = reservedGifts.value.find { it.reservation.giftId == giftId }?.reservation?.id
    fun getWishlists() = wishlists.value
    fun getWishlist(id: Int) = wishlists.value.find { it.id == id }
    fun getAllGifts() = wishlists.value.flatMap { it.gifts }
    fun getGift(id: Int) = getAllGifts().find { it.id == id }
    fun getGiftById(id: Int) = getGift(id)
    fun getGiftsByWishlistId(id: Int) = getWishlist(id)?.gifts.orEmpty()
    fun loadMyWishlists() {
        if (wishlistsState == LoadState.Loading) return
        wishlistsState = LoadState.Loading
        viewModelScope.launch {
            when (val result = wishlistRepository.getMyWishlists()) {
                is AppResult.Success -> wishlistsState = LoadState.Content(result.data)
                is AppResult.Failure -> wishlistsState = LoadState.Error(result.error.toUserMessage())
            }
        }
    }
    fun loadWishlist(id: Int) {
        if (wishlistState == LoadState.Loading) return
        wishlistState = LoadState.Loading
        viewModelScope.launch {
            when (val result = wishlistRepository.getWishlist(id)) {
                is AppResult.Success -> wishlistState = LoadState.Content(result.data)
                is AppResult.Failure -> wishlistState = LoadState.Error(result.error.toUserMessage())
            }
        }
    }
    suspend fun createWishlistAndWait(title: String, isPrivate: Boolean, eventDate: String): AppResult<Wishlist> {
        return mutate { createWishlistAction(title, isPrivate, eventDate) }
    }
    fun createWishlist(title: String, description: String, isPrivate: Boolean, eventDate: String) =
        viewModelScope.launch { createWishlistAndWait(title, isPrivate, eventDate) }
    fun addWishlist(title: String, description: String, isPrivate: Boolean, eventDate: String) = createWishlist(title, description, isPrivate, eventDate)
    suspend fun deleteWishlistAndWait(id: Int): AppResult<Unit> = mutate { wishlistRepository.deleteWishlist(id) }
    fun deleteWishlist(id: Int) = viewModelScope.launch { deleteWishlistAndWait(id) }
    suspend fun findWishlist(link: String): AppResult<Wishlist> {
        val result = wishlistRepository.findByPublicLink(link)
        return result
    }
    suspend fun addGiftAndWait(name: String, price: String, link: String, description: String, wishlistId: Int): AppResult<Gift> {
        return mutate { addGiftAction(wishlistId, name, price, description, link) }
    }
    fun addGift(name: String, price: String, link: String, description: String, wishlistId: Int) =
        viewModelScope.launch { addGiftAndWait(name, price, link, description, wishlistId) }
    suspend fun deleteGiftAndWait(id: Int): AppResult<Unit> = mutate { giftRepository.deleteGift(id) }
    fun deleteGift(id: Int) = viewModelScope.launch { deleteGiftAndWait(id) }
    suspend fun reserveGiftAndWait(giftId: Int): AppResult<Reservation> = mutate { reserveGiftAction(giftId) }
    fun reserveGift(giftId: Int) = viewModelScope.launch { reserveGiftAndWait(giftId) }
    fun reserveGift(wishlistId: Int, giftId: Int, userName: String) = reserveGift(giftId)
    suspend fun cancelReservationAndWait(reservationId: Int): AppResult<Reservation> = mutate { cancelReservationAction(reservationId) }
    fun cancelReservation(reservationId: Int) = viewModelScope.launch { cancelReservationAndWait(reservationId) }
    private suspend fun <T> mutate(action: suspend () -> AppResult<T>): AppResult<T> {
        if (wishlistsState == LoadState.Loading) return AppResult.Failure(AppError.Conflict)
        wishlistsState = LoadState.Loading
        return action().also(::finish)
    }
    private fun finish(result: AppResult<*>) { wishlistsState = when (result) { is AppResult.Success -> LoadState.Content(wishlists.value); is AppResult.Failure -> LoadState.Error(errorMessage(result)) } }
    private fun errorMessage(result: AppResult.Failure) = result.error.toUserMessage()
}
