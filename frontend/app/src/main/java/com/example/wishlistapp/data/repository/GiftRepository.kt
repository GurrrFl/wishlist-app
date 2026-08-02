package com.example.wishlistapp.data.repository

import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.result.AppResult

interface GiftRepository {
    suspend fun getByWishlist(wishlistId: Int): AppResult<List<Gift>>
    suspend fun getGift(id: Int): AppResult<Gift>
    suspend fun createGift(wishlistId: Int, name: String, price: Double?, description: String?, link: String?): AppResult<Gift>
    suspend fun updateGift(giftId: Int, name: String?, price: Double?, description: String?, link: String?): AppResult<Gift>
    suspend fun deleteGift(giftId: Int): AppResult<Unit>
}
