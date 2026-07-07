package com.example.wishlistapp.data.repository

import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import com.example.wishlistapp.data.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeWishlistRepository(private val store: FakeDataStore, private val sessionManager: SessionManager) : WishlistRepository {
    private val publicWishlist = MutableStateFlow<Wishlist?>(null)
    override fun observeMyWishlists(): Flow<List<Wishlist>> = store.wishlists.map { lists ->
        lists.filter { it.ownerId == currentUserId }
    }
    override fun observePublicWishlist(): Flow<Wishlist?> = publicWishlist.asStateFlow()
    override suspend fun getMyWishlists(): AppResult<List<Wishlist>> =
        AppResult.Success(store.wishlists.value.filter { it.ownerId == currentUserId })
    override suspend fun getWishlist(id: Int): AppResult<Wishlist> {
        val wishlist = store.wishlists.value.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(wishlist)
    }
    override suspend fun findByPublicLink(link: String): AppResult<Wishlist> {
        val wishlist = store.wishlists.value.find { !it.isPrivate && it.publicLink == link } ?: return AppResult.Failure(AppError.NotFound)
        publicWishlist.value = wishlist
        return AppResult.Success(wishlist)
    }
    override suspend fun createWishlist(title: String, eventDate: LocalDate, isPrivate: Boolean): AppResult<Wishlist> {
        val wishlist = Wishlist(store.nextWishlistId++, title, currentUserId, sessionManager.getUserName() ?: "User", eventDate, null, isPrivate, if (isPrivate) null else "wishlist.app/${10000 + store.nextWishlistId}")
        store.updateWishlists { it + wishlist }; return AppResult.Success(wishlist)
    }
    override suspend fun updateWishlist(id: Int, title: String?, eventDate: LocalDate?, isPrivate: Boolean?): AppResult<Wishlist> {
        val old = store.wishlists.value.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound)
        val updated = old.copy(title = title ?: old.title, eventDate = eventDate ?: old.eventDate, isPrivate = isPrivate ?: old.isPrivate, publicLink = if (isPrivate == true) null else old.publicLink)
        store.updateWishlists { it.map { item -> if (item.id == id) updated else item } }; return AppResult.Success(updated)
    }
    override suspend fun deleteWishlist(id: Int): AppResult<Unit> { if (store.wishlists.value.none { it.id == id }) return AppResult.Failure(AppError.NotFound); store.updateWishlists { it.filterNot { item -> item.id == id } }; return AppResult.Success(Unit) }
    private val currentUserId get() = sessionManager.getUserId() ?: 0
}
