package com.example.wishlistapp.data.repository

import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WishlistRepository {
    fun observeMyWishlists(): Flow<List<Wishlist>>
    suspend fun getMyWishlists(): AppResult<List<Wishlist>>
    suspend fun getWishlist(id: Int): AppResult<Wishlist>
    suspend fun findByPublicLink(link: String): AppResult<Wishlist>
    suspend fun createWishlist(title: String, eventDate: LocalDate, isPrivate: Boolean): AppResult<Wishlist>
    suspend fun updateWishlist(id: Int, title: String?, eventDate: LocalDate?, isPrivate: Boolean?): AppResult<Wishlist>
    suspend fun deleteWishlist(id: Int): AppResult<Unit>
}
