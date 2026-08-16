package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.mapper.toDomain
import com.example.wishlistapp.data.model.dto.CreateGiftDto
import com.example.wishlistapp.data.model.dto.UpdateGiftDto
import com.example.wishlistapp.data.remote.GiftApi
import com.example.wishlistapp.data.remote.remoteCall
import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.result.AppResult

class NetworkGiftRepository(private val api: GiftApi, private val wishlists: NetworkWishlistRepository) : GiftRepository {
    override suspend fun getByWishlist(wishlistId: Int): AppResult<List<Gift>> =
        remoteCall { api.getByWishlist(wishlistId).map { it.toDomain() } }

    override suspend fun getGift(id: Int): AppResult<Gift> = remoteCall { api.getGift(id).toDomain() }

    override suspend fun createGift(
        wishlistId: Int, name: String, price: Double?, description: String?, link: String?
    ): AppResult<Gift> {
        val result = remoteCall { api.create(CreateGiftDto(wishlistId, name, price, description, link)).toDomain() }
        if (result is AppResult.Success) wishlists.refreshWishlist(wishlistId)
        return result
    }

    override suspend fun updateGift(
        giftId: Int, name: String?, price: Double?, description: String?, link: String?
    ): AppResult<Gift> {
        val result = remoteCall { api.update(giftId, UpdateGiftDto(name, price, description, link)).toDomain() }
        if (result is AppResult.Success) wishlists.refreshWishlist(result.data.wishlistId)
        return result
    }

    override suspend fun deleteGift(giftId: Int): AppResult<Unit> {
        val wishlistId = wishlists.cachedWishlistIdForGift(giftId)
        val result = remoteCall { api.delete(giftId) }
        if (result is AppResult.Success && wishlistId != null) wishlists.refreshWishlist(wishlistId)
        return result
    }
}
