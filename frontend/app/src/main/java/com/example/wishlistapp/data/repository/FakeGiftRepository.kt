package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import java.time.LocalDateTime

class FakeGiftRepository(private val store: FakeDataStore, private val session: SessionManager) : GiftRepository {
    override suspend fun getByWishlist(wishlistId: Int): AppResult<List<Gift>> {
        val wishlist = store.wishlists.value.find { it.id == wishlistId } ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(wishlist.gifts)
    }
    override suspend fun getGift(id: Int): AppResult<Gift> {
        val gift = store.wishlists.value.flatMap { it.gifts }.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(gift)
    }
    override suspend fun createGift(wishlistId: Int, name: String, price: Double?, description: String?, link: String?): AppResult<Gift> {
        val wishlist = store.wishlists.value.find { it.id == wishlistId } ?: return AppResult.Failure(AppError.NotFound)
        if (wishlist.ownerId != session.getUserId()) return AppResult.Failure(AppError.Forbidden)
        val gift = Gift(store.nextGiftId++, wishlistId, name, price, description, link, GiftStatus.AVAILABLE, LocalDateTime.now(), session.getUserName())
        store.updateWishlists { it.map { list -> if (list.id == wishlistId) list.copy(gifts = list.gifts + gift) else list } }; return AppResult.Success(gift)
    }
    override suspend fun updateGift(giftId: Int, name: String?, price: Double?, description: String?, link: String?): AppResult<Gift> {
        val gift = (getGift(giftId) as? AppResult.Success)?.data ?: return AppResult.Failure(AppError.NotFound)
        if (!isOwner(gift)) return AppResult.Failure(AppError.Forbidden)
        return change(giftId) { it.copy(name = name ?: it.name, price = price ?: it.price, description = description ?: it.description, link = link ?: it.link) }
    }
    override suspend fun deleteGift(giftId: Int): AppResult<Unit> {
        val gift = (getGift(giftId) as? AppResult.Success)?.data ?: return AppResult.Failure(AppError.NotFound)
        if (!isOwner(gift)) return AppResult.Failure(AppError.Forbidden)
        if (gift.status == GiftStatus.RESERVED) return AppResult.Failure(AppError.Conflict)
        store.updateWishlists { lists -> lists.map { list -> if (list.id == gift.wishlistId) list.copy(gifts = list.gifts.filterNot { it.id == giftId }) else list } }
        return AppResult.Success(Unit)
    }
    suspend fun change(id: Int, transform: (Gift) -> Gift): AppResult<Gift> { val old = (getGift(id) as? AppResult.Success)?.data ?: return AppResult.Failure(AppError.NotFound); val updated = transform(old); store.updateWishlists { lists -> lists.map { list -> if (list.id == old.wishlistId) list.copy(gifts = list.gifts.map { if (it.id == id) updated else it }) else list } }; return AppResult.Success(updated) }
    private fun isOwner(gift: Gift): Boolean = store.wishlists.value
        .find { it.id == gift.wishlistId }?.ownerId == session.getUserId()
}
