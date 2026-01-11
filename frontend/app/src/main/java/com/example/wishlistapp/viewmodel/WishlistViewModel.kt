package com.example.wishlistapp.viewmodel


import androidx.lifecycle.ViewModel
import com.example.wishlistapp.data.model.Wishlist
import com.example.wishlistapp.data.repository.WishlistRepository


class WishlistViewModel : ViewModel() {

    fun getWishlists(): List<Wishlist> =
        WishlistRepository.getWishlists()

    fun getWishlist(id: Int): Wishlist? =
        WishlistRepository.getWishlistById(id)

    fun reserveGift(wishlistId: Int, giftId: Int, userName: String) {
        WishlistRepository.reserveGift(
            wishlistId = wishlistId,
            giftId = giftId,
            userName = userName
        )
    }
    fun addWishlist(wishlist: Wishlist) {
        WishlistRepository.addWishlist(wishlist)
    }
    fun deleteWishlist(wishlistId: Int) {
        WishlistRepository.deleteWishlist(wishlistId)
    }
}
