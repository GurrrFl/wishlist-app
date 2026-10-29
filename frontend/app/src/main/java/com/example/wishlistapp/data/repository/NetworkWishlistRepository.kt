package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.mapper.toDomain
import com.example.wishlistapp.data.model.dto.CreateWishlistDto
import com.example.wishlistapp.data.model.dto.UpdateWishlistDto
import com.example.wishlistapp.data.remote.WishlistApi
import com.example.wishlistapp.data.remote.AuthApi
import com.example.wishlistapp.data.remote.remoteCall
import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.domain.model.publicLinkCode
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class NetworkWishlistRepository(private val api: WishlistApi, private val profileApi: AuthApi, private val session: SessionManager) : WishlistRepository {
    private val cache = MutableStateFlow<List<Wishlist>>(emptyList())
    private val publicCache = MutableStateFlow<Pair<Int?, Wishlist>?>(null)
    override fun observeMyWishlists() = combine(cache, session.userId) { lists, userId ->
        lists.filter { it.ownerId == userId }
    }
    override fun observePublicWishlist() = combine(publicCache, session.userId) { cached, userId ->
        cached?.takeIf { it.first == userId }?.second
    }

    override suspend fun getMyWishlists(): AppResult<List<Wishlist>> {
        val result = remoteCall {
            profileApi.getProfile().wishlists.map { summary ->
                api.getWishlist(summary.wishlistId).toDomain()
            }
        }
        if (result is AppResult.Success) cache.value = result.data
        return result
    }

    override suspend fun getWishlist(id: Int): AppResult<Wishlist> {
        val result = remoteCall { api.getWishlist(id).toDomain() }
        if (result is AppResult.Success && result.data.ownerId == session.getUserId()) {
            cache.value = cache.value.filterNot { it.id == id } + result.data
        }
        return result
    }

    override suspend fun refreshWishlist(id: Int): AppResult<Wishlist> {
        val public = publicCache.value?.takeIf { it.first == session.getUserId() && it.second.id == id }?.second
        val result = if (public != null && public.ownerId != session.getUserId()) {
            val link = public.publicLink ?: return AppResult.Failure(AppError.NotFound)
            remoteCall { api.findByPublicLink(publicLinkCode(link)).toDomain() }
        } else getWishlist(id)
        if (result is AppResult.Success && public != null) publicCache.value = session.getUserId() to result.data
        return result
    }
    override suspend fun findByPublicLink(link: String): AppResult<Wishlist> {
        val result = remoteCall { api.findByPublicLink(publicLinkCode(link)).toDomain() }
        if (result is AppResult.Success) publicCache.value = session.getUserId() to result.data
        return result
    }

    override suspend fun createWishlist(title: String, eventDate: LocalDate, isPrivate: Boolean): AppResult<Wishlist> {
        val userId = session.getUserId() ?: return AppResult.Failure(AppError.Unauthorized)
        val result = remoteCall { api.create(CreateWishlistDto(title, eventDate.toString(), isPrivate, userId)).toDomain() }
        if (result is AppResult.Success) cache.value = cache.value + result.data
        return result
    }

    override suspend fun updateWishlist(id: Int, title: String?, eventDate: LocalDate?, isPrivate: Boolean?): AppResult<Wishlist> {
        val updated = remoteCall { api.update(id, UpdateWishlistDto(title, eventDate?.toString(), isPrivate)).toDomain() }
        if (updated !is AppResult.Success) return updated
        val result = if (isPrivate == false && updated.data.publicLink == null) {
            remoteCall { api.regenerateLink(id).toDomain() }
        } else updated
        val visible = (result as? AppResult.Success)?.data ?: updated.data
        cache.value = cache.value.map { if (it.id == id) visible else it }
        return result
    }

    override suspend fun deleteWishlist(id: Int): AppResult<Unit> {
        val result = remoteCall { api.delete(id) }
        if (result is AppResult.Success) cache.value = cache.value.filterNot { it.id == id }
        return result
    }
}
