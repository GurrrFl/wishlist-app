package com.example.wishlistapp.data.repository

import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.domain.model.publicLinkCode
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import com.example.wishlistapp.data.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class FakeWishlistRepository(private val store: FakeDataStore, private val sessionManager: SessionManager) : WishlistRepository {
    private val publicWishlist = MutableStateFlow<Pair<Int?, Wishlist>?>(null)
    override fun observeMyWishlists(): Flow<List<Wishlist>> = combine(store.wishlists, sessionManager.userId) { lists, userId ->
        lists.filter { it.ownerId == userId }
    }
    override fun observePublicWishlist(): Flow<Wishlist?> = combine(publicWishlist, sessionManager.userId) { cached, userId ->
        cached?.takeIf { it.first == userId }?.second
    }
    override suspend fun getMyWishlists(): AppResult<List<Wishlist>> =
        AppResult.Success(store.wishlists.value.filter { it.ownerId == currentUserId })
    override suspend fun getWishlist(id: Int): AppResult<Wishlist> {
        val wishlist = store.wishlists.value.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound)
        if (wishlist.ownerId != currentUserId) return AppResult.Failure(AppError.Forbidden)
        return AppResult.Success(wishlist)
    }
    override suspend fun findByPublicLink(link: String): AppResult<Wishlist> {
        val code = publicLinkCode(link)
        val wishlist = store.wishlists.value.find {
            !it.isPrivate && it.publicLink?.let(::publicLinkCode) == code
        } ?: return AppResult.Failure(AppError.NotFound)
        publicWishlist.value = sessionManager.getUserId() to wishlist
        return AppResult.Success(wishlist)
    }
    override suspend fun createWishlist(title: String, eventDate: LocalDate, isPrivate: Boolean): AppResult<Wishlist> {
        val ownerId = sessionManager.getUserId() ?: return AppResult.Failure(AppError.Unauthorized)
        val wishlist = Wishlist(store.nextWishlistId++, title, ownerId, sessionManager.getUserName() ?: "User", eventDate, null, isPrivate, if (isPrivate) null else "wishlist.app/${10000 + store.nextWishlistId}")
        store.updateWishlists { it + wishlist }; return AppResult.Success(wishlist)
    }
    override suspend fun updateWishlist(id: Int, title: String?, eventDate: LocalDate?, isPrivate: Boolean?): AppResult<Wishlist> {
        val old = store.wishlists.value.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound)
        if (old.ownerId != currentUserId) return AppResult.Failure(AppError.Forbidden)
        val updated = old.copy(
            title = title ?: old.title,
            eventDate = eventDate ?: old.eventDate,
            isPrivate = isPrivate ?: old.isPrivate,
            publicLink = when (isPrivate) {
                true -> null
                false -> old.publicLink ?: "wishlist.app/${10000 + old.id}"
                null -> old.publicLink
            }
        )
        store.updateWishlists { it.map { item -> if (item.id == id) updated else item } }; return AppResult.Success(updated)
    }
    override suspend fun deleteWishlist(id: Int): AppResult<Unit> {
        val wishlist = store.wishlists.value.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound)
        if (wishlist.ownerId != currentUserId) return AppResult.Failure(AppError.Forbidden)
        store.updateWishlists { it.filterNot { item -> item.id == id } }
        return AppResult.Success(Unit)
    }
    private val currentUserId get() = sessionManager.getUserId() ?: 0
}
