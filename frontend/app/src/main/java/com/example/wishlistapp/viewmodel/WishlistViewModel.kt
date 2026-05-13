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
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed interface LoadState<out T> { data object Idle : LoadState<Nothing>; data object Loading : LoadState<Nothing>; data class Content<T>(val data: T) : LoadState<T>; data class Error(val message: String) : LoadState<Nothing> }
data class ReservedGift(val reservation: Reservation, val gift: Gift)

class WishlistViewModel(private val wishlistRepository: WishlistRepository, private val giftRepository: GiftRepository, private val reservationRepository: ReservationRepository, private val sessionManager: SessionManager, private val createWishlistAction: CreateWishlistAction, private val addGiftAction: AddGiftAction, private val reserveGiftAction: ReserveGiftAction, private val cancelReservationAction: CancelReservationAction) : ViewModel() {
    private val _publicWishlist = kotlinx.coroutines.flow.MutableStateFlow<Wishlist?>(null)
    val publicWishlist: StateFlow<Wishlist?> = _publicWishlist
    init {
        loadMyWishlists()
        viewModelScope.launch { reservationRepository.getMyReservations() }
    }
    val wishlists: StateFlow<List<Wishlist>> = wishlistRepository.observeMyWishlists().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val reservedGifts: StateFlow<List<ReservedGift>> = combine(wishlists, publicWishlist, reservationRepository.observeMyReservations()) { lists, public, reservations ->
        val giftsById = (lists.flatMap { it.gifts } + public?.gifts.orEmpty()).associateBy { it.id }
        reservations.mapNotNull { reservation -> giftsById[reservation.giftId]?.let { ReservedGift(reservation, it) } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    var wishlistsState by mutableStateOf<LoadState<List<Wishlist>>>(LoadState.Idle); private set
    var wishlistState by mutableStateOf<LoadState<Wishlist>>(LoadState.Idle); private set

    fun isWishlistOwner(wishlistId: Int) = getWishlist(wishlistId)?.ownerId == sessionManager.getUserId()
    fun reservationIdForGift(giftId: Int) = reservedGifts.value.find { it.gift.id == giftId }?.reservation?.id
    fun getWishlists() = wishlists.value
    fun getWishlist(id: Int) = wishlists.value.find { it.id == id }
    fun getAllGifts() = wishlists.value.flatMap { it.gifts }
    fun getGift(id: Int) = getAllGifts().find { it.id == id }
    fun getGiftById(id: Int) = getGift(id)
    fun getGiftsByWishlistId(id: Int) = getWishlist(id)?.gifts.orEmpty()
    fun loadMyWishlists() = viewModelScope.launch { wishlistsState = LoadState.Loading; when (val r = wishlistRepository.getMyWishlists()) { is AppResult.Success -> wishlistsState = LoadState.Content(r.data); is AppResult.Failure -> wishlistsState = LoadState.Error("Не удалось загрузить списки") } }
    fun loadWishlist(id: Int) = viewModelScope.launch { wishlistState = LoadState.Loading; when (val r = wishlistRepository.getWishlist(id)) { is AppResult.Success -> wishlistState = LoadState.Content(r.data); is AppResult.Failure -> wishlistState = LoadState.Error("Не удалось загрузить список") } }
    fun createWishlist(title: String, description: String, isPrivate: Boolean, eventDate: String) = viewModelScope.launch { wishlistsState = LoadState.Loading; finish(createWishlistAction(title, isPrivate, eventDate)) }
    fun addWishlist(title: String, description: String, isPrivate: Boolean, eventDate: String) = createWishlist(title, description, isPrivate, eventDate)
    fun deleteWishlist(id: Int) = viewModelScope.launch { wishlistsState = LoadState.Loading; finish(wishlistRepository.deleteWishlist(id)) }
    suspend fun findWishlist(link: String): AppResult<Wishlist> {
        val result = wishlistRepository.findByPublicLink(link)
        if (result is AppResult.Success) _publicWishlist.value = result.data
        return result
    }
    fun addGift(name: String, price: String, link: String, description: String, wishlistId: Int) = viewModelScope.launch { wishlistsState = LoadState.Loading; finish(addGiftAction(wishlistId, name, price, description, link)) }
    fun deleteGift(id: Int) = viewModelScope.launch { wishlistsState = LoadState.Loading; finish(giftRepository.deleteGift(id)) }
    fun reserveGift(giftId: Int) = viewModelScope.launch { wishlistsState = LoadState.Loading; finish(reserveGiftAction(giftId)) }
    fun reserveGift(wishlistId: Int, giftId: Int, userName: String) = reserveGift(giftId)
    fun cancelReservation(reservationId: Int) = viewModelScope.launch { wishlistsState = LoadState.Loading; finish(cancelReservationAction(reservationId)) }
    private fun finish(result: AppResult<*>) { wishlistsState = when (result) { is AppResult.Success -> LoadState.Content(wishlists.value); is AppResult.Failure -> LoadState.Error(errorMessage(result)) } }
    private fun errorMessage(result: AppResult.Failure) = result.error.toUserMessage()
}
