package com.example.wishlistapp.data.repository

import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class FakeWishlistRepository(private val store: FakeDataStore) : WishlistRepository {
    override fun observeMyWishlists(): Flow<List<Wishlist>> = store.wishlists
    override suspend fun getMyWishlists(): AppResult<List<Wishlist>> = AppResult.Success(store.wishlists.value)
    override suspend fun getWishlist(id: Int): AppResult<Wishlist> {
        val wishlist = store.wishlists.value.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(wishlist)
    }
    override suspend fun findByPublicLink(link: String): AppResult<Wishlist> {
        val wishlist = store.wishlists.value.find { !it.isPrivate && it.publicLink == link } ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(wishlist)
    }
    override suspend fun createWishlist(title: String, eventDate: LocalDate, isPrivate: Boolean): AppResult<Wishlist> {
        val wishlist = Wishlist(store.nextWishlistId++, title, "User", eventDate, null, isPrivate, if (isPrivate) null else "wishlist.app/${10000 + store.nextWishlistId}")
        store.updateWishlists { it + wishlist }; return AppResult.Success(wishlist)
    }
    override suspend fun updateWishlist(id: Int, title: String?, eventDate: LocalDate?, isPrivate: Boolean?): AppResult<Wishlist> {
        val old = store.wishlists.value.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound)
        val updated = old.copy(title = title ?: old.title, eventDate = eventDate ?: old.eventDate, isPrivate = isPrivate ?: old.isPrivate, publicLink = if (isPrivate == true) null else old.publicLink)
        store.updateWishlists { it.map { item -> if (item.id == id) updated else item } }; return AppResult.Success(updated)
    }
    override suspend fun deleteWishlist(id: Int): AppResult<Unit> { if (store.wishlists.value.none { it.id == id }) return AppResult.Failure(AppError.NotFound); store.updateWishlists { it.filterNot { item -> item.id == id } }; return AppResult.Success(Unit) }
}
