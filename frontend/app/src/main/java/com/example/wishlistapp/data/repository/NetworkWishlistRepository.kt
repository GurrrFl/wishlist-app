package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.mapper.toDomain
import com.example.wishlistapp.data.model.dto.CreateWishlistDto
import com.example.wishlistapp.data.model.dto.UpdateWishlistDto
import com.example.wishlistapp.data.remote.WishlistApi
import com.example.wishlistapp.data.remote.AuthApi
import com.example.wishlistapp.data.remote.remoteCall
import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class NetworkWishlistRepository(private val api: WishlistApi, private val profileApi: AuthApi, private val session: SessionManager) : WishlistRepository {
    private val cache = MutableStateFlow<List<Wishlist>>(emptyList())
    private val publicCache = MutableStateFlow<Wishlist?>(null)
    override fun observeMyWishlists() = cache.asStateFlow()
    override fun observePublicWishlist() = publicCache.asStateFlow()

    override suspend fun getMyWishlists(): AppResult<List<Wishlist>> {
        val result = remoteCall {
            profileApi.getProfile().wishlists.map { summary ->
                api.getWishlist(summary.wishlistId).toDomain()
            }
        }
        if (result is AppResult.Success) cache.value = result.data
        return result
    }

    override suspend fun getWishlist(id: Int): AppResult<Wishlist> = remoteCall { api.getWishlist(id).toDomain() }

    suspend fun refreshWishlist(id: Int): AppResult<Wishlist> {
        val result = getWishlist(id)
        if (result is AppResult.Success) {
            cache.value = cache.value.map { if (it.id == id) result.data else it }
        }
        return result
    }
    override suspend fun findByPublicLink(link: String): AppResult<Wishlist> {
        val result = remoteCall { api.findByPublicLink(link.substringAfterLast('/')).toDomain() }
        if (result is AppResult.Success) publicCache.value = result.data
        return result
    }

    override suspend fun createWishlist(title: String, eventDate: LocalDate, isPrivate: Boolean): AppResult<Wishlist> {
        val userId = session.getUserId() ?: return AppResult.Failure(AppError.Unauthorized)
        val result = remoteCall { api.create(CreateWishlistDto(title, eventDate.toString(), isPrivate, userId)).toDomain() }
        if (result is AppResult.Success) cache.value = cache.value + result.data
        return result
    }

    override suspend fun updateWishlist(id: Int, title: String?, eventDate: LocalDate?, isPrivate: Boolean?): AppResult<Wishlist> {
        val result = remoteCall { api.update(id, UpdateWishlistDto(title, eventDate?.toString(), isPrivate)).toDomain() }
        if (result is AppResult.Success) cache.value = cache.value.map { if (it.id == id) result.data else it }
        return result
    }

    override suspend fun deleteWishlist(id: Int): AppResult<Unit> {
        val result = remoteCall { api.delete(id) }
        if (result is AppResult.Success) cache.value = cache.value.filterNot { it.id == id }
        return result
    }
}
