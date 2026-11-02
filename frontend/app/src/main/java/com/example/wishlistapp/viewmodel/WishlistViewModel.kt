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
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LoadState<out T> {
    data object Idle : LoadState<Nothing>
    data object Loading : LoadState<Nothing>
    data class Content<T>(val data: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}

data class ReservedGift(val reservation: Reservation, val gift: Gift?)

class WishlistViewModel(
    private val wishlistRepository: WishlistRepository,
    private val giftRepository: GiftRepository,
    private val reservationRepository: ReservationRepository,
    private val sessionManager: SessionManager,
    private val createWishlistAction: CreateWishlistAction,
    private val addGiftAction: AddGiftAction,
    private val reserveGiftAction: ReserveGiftAction,
    private val cancelReservationAction: CancelReservationAction
) : ViewModel() {
    val publicWishlist: StateFlow<Wishlist?> = wishlistRepository.observePublicWishlist()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val wishlists: StateFlow<List<Wishlist>> = wishlistRepository.observeMyWishlists()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val reservedGifts: StateFlow<List<ReservedGift>> =
        combine(wishlists, publicWishlist, reservationRepository.observeMyReservations()) { lists, public, reservations ->
            val giftsById = (lists.flatMap { it.gifts } + public?.gifts.orEmpty()).associateBy { it.id }
            reservations.map { ReservedGift(it, giftsById[it.giftId]) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var wishlistsState by mutableStateOf<LoadState<List<Wishlist>>>(LoadState.Idle); private set
    var wishlistState by mutableStateOf<LoadState<Wishlist>>(LoadState.Idle); private set
    var searchState by mutableStateOf<LoadState<Wishlist>>(LoadState.Idle); private set
    var mutationState by mutableStateOf<LoadState<Unit>>(LoadState.Idle); private set
    val isMutating get() = mutationState == LoadState.Loading

    private val mutationInProgress = AtomicBoolean(false)
    private val searchInProgress = AtomicBoolean(false)
    private var listJob: Job? = null
    private var detailJob: Job? = null
    private var listGeneration = 0
    private var detailGeneration = 0
    private var selectedWishlistId: Int? = null

    init {
        loadMyWishlists()
        viewModelScope.launch { reservationRepository.getMyReservations() }
        viewModelScope.launch {
            sessionManager.userId.drop(1).collect {
                listGeneration++
                detailGeneration++
                listJob?.cancel()
                detailJob?.cancel()
                wishlistsState = LoadState.Idle
                wishlistState = LoadState.Idle
                searchState = LoadState.Idle
                selectedWishlistId = null
                loadMyWishlists()
                reservationRepository.getMyReservations()
            }
        }
    }

    fun isWishlistOwner(wishlistId: Int): Boolean =
        sessionManager.getUserId()?.let { findCachedWishlist(wishlistId)?.ownerId == it } == true

    fun reservationIdForGift(giftId: Int) =
        reservedGifts.value.find { it.reservation.giftId == giftId }?.reservation?.id

    fun getWishlists() = wishlists.value
    fun findCachedWishlist(id: Int): Wishlist? = wishlists.value.find { it.id == id }
        ?: publicWishlist.value?.takeIf { it.id == id }
    fun getWishlist(id: Int) = findCachedWishlist(id)
    fun getAllGifts() = wishlists.value.flatMap { it.gifts }
    fun findCachedGift(id: Int): Gift? = getAllGifts().find { it.id == id }
        ?: publicWishlist.value?.gifts?.find { it.id == id }
    fun getGift(id: Int) = findCachedGift(id)
    fun getGiftById(id: Int) = findCachedGift(id)
    fun getGiftsByWishlistId(id: Int) = findCachedWishlist(id)?.gifts.orEmpty()

    fun loadMyWishlists() {
        if (listJob?.isActive == true) return
        val generation = ++listGeneration
        wishlistsState = LoadState.Loading
        listJob = viewModelScope.launch {
            try {
                val loaded = when (val result = wishlistRepository.getMyWishlists()) {
                    is AppResult.Success -> LoadState.Content(result.data)
                    is AppResult.Failure -> LoadState.Error(result.error.toUserMessage())
                }
                if (listGeneration == generation) wishlistsState = loaded
            } finally {
                if (listGeneration == generation && wishlistsState == LoadState.Loading) wishlistsState = LoadState.Idle
            }
        }
    }

    fun loadWishlist(id: Int) {
        if (selectedWishlistId == id && detailJob?.isActive == true) return
        val generation = ++detailGeneration
        detailJob?.cancel()
        selectedWishlistId = id
        wishlistState = LoadState.Loading
        detailJob = viewModelScope.launch {
            try {
                val loaded = when (val result = wishlistRepository.getWishlist(id)) {
                    is AppResult.Success -> LoadState.Content(result.data)
                    is AppResult.Failure -> LoadState.Error(result.error.toUserMessage())
                }
                if (detailGeneration == generation) wishlistState = loaded
            } finally {
                if (detailGeneration == generation && wishlistState == LoadState.Loading) wishlistState = LoadState.Idle
            }
        }
    }

    suspend fun findWishlist(link: String): AppResult<Wishlist> {
        if (!searchInProgress.compareAndSet(false, true)) return AppResult.Failure(AppError.Conflict)
        searchState = LoadState.Loading
        try {
            return wishlistRepository.findByPublicLink(link).also { result ->
                searchState = when (result) {
                    is AppResult.Success -> LoadState.Content(result.data)
                    is AppResult.Failure -> LoadState.Error(result.error.toUserMessage())
                }
            }
        } finally {
            searchInProgress.set(false)
            if (searchState == LoadState.Loading) searchState = LoadState.Idle
        }
    }

    suspend fun createWishlistAndWait(title: String, isPrivate: Boolean, eventDate: String): AppResult<Wishlist> =
        mutate({ createWishlistAction(title, isPrivate, eventDate) }) { created -> refreshWishlist(created.id) }

    fun createWishlist(title: String, description: String, isPrivate: Boolean, eventDate: String) =
        viewModelScope.launch { createWishlistAndWait(title, isPrivate, eventDate) }
    fun addWishlist(title: String, description: String, isPrivate: Boolean, eventDate: String) =
        createWishlist(title, description, isPrivate, eventDate)

    suspend fun deleteWishlistAndWait(id: Int): AppResult<Unit> = mutate({ wishlistRepository.deleteWishlist(id) }) {
        if (selectedWishlistId == id) wishlistState = LoadState.Idle
        reloadMyWishlists()
    }
    fun deleteWishlist(id: Int) = viewModelScope.launch { deleteWishlistAndWait(id) }

    suspend fun addGiftAndWait(name: String, price: String, link: String, description: String, wishlistId: Int): AppResult<Gift> =
        mutate({ addGiftAction(wishlistId, name, price, description, link) }) { refreshWishlist(wishlistId) }
    fun addGift(name: String, price: String, link: String, description: String, wishlistId: Int) =
        viewModelScope.launch { addGiftAndWait(name, price, link, description, wishlistId) }

    suspend fun deleteGiftAndWait(id: Int): AppResult<Unit> {
        val wishlistId = findCachedGift(id)?.wishlistId ?: when (val gift = giftRepository.getGift(id)) {
            is AppResult.Success -> gift.data.wishlistId
            is AppResult.Failure -> return AppResult.Failure(gift.error)
        }
        return mutate({ giftRepository.deleteGift(id) }) { refreshWishlist(wishlistId) }
    }
    fun deleteGift(id: Int) = viewModelScope.launch { deleteGiftAndWait(id) }

    suspend fun reserveGiftAndWait(giftId: Int): AppResult<Reservation> {
        val wishlistId = findCachedGift(giftId)?.wishlistId
        return mutate({ reserveGiftAction(giftId) }) {
            reservationRepository.getMyReservations()
            if (wishlistId != null) refreshWishlist(wishlistId)
        }
    }
    fun reserveGift(giftId: Int) = viewModelScope.launch { reserveGiftAndWait(giftId) }
    fun reserveGift(wishlistId: Int, giftId: Int, userName: String) = reserveGift(giftId)

    suspend fun cancelReservationAndWait(reservationId: Int): AppResult<Reservation> {
        val giftId = reservedGifts.value.find { it.reservation.id == reservationId }?.reservation?.giftId
        val wishlistId = giftId?.let { findCachedGift(it)?.wishlistId }
        return mutate({ cancelReservationAction(reservationId) }) {
            reservationRepository.getMyReservations()
            if (wishlistId != null) refreshWishlist(wishlistId)
        }
    }
    fun cancelReservation(reservationId: Int) = viewModelScope.launch { cancelReservationAndWait(reservationId) }

    private suspend fun refreshWishlist(id: Int) {
        when (val refreshed = wishlistRepository.refreshWishlist(id)) {
            is AppResult.Success -> if (selectedWishlistId == id) wishlistState = LoadState.Content(refreshed.data)
            is AppResult.Failure -> mutationState = LoadState.Error("Изменение сохранено, но список не удалось обновить")
        }
    }

    private suspend fun reloadMyWishlists() {
        wishlistsState = when (val result = wishlistRepository.getMyWishlists()) {
            is AppResult.Success -> LoadState.Content(result.data)
            is AppResult.Failure -> LoadState.Error(result.error.toUserMessage())
        }
    }

    private suspend fun <T> mutate(action: suspend () -> AppResult<T>, onSuccess: suspend (T) -> Unit = {}): AppResult<T> {
        if (!mutationInProgress.compareAndSet(false, true)) return AppResult.Failure(AppError.Conflict)
        mutationState = LoadState.Loading
        try {
            return action().also { result ->
                when (result) {
                    is AppResult.Success -> {
                        onSuccess(result.data)
                        if (mutationState == LoadState.Loading) mutationState = LoadState.Content(Unit)
                    }
                    is AppResult.Failure -> mutationState = LoadState.Error(result.error.toUserMessage())
                }
            }
        } finally {
            mutationInProgress.set(false)
            if (mutationState == LoadState.Loading) mutationState = LoadState.Idle
        }
    }
}
