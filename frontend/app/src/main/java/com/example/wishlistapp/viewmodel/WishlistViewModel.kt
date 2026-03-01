package com.example.wishlistapp.viewmodel


import androidx.lifecycle.ViewModel
import com.example.wishlistapp.data.model.Gift
import com.example.wishlistapp.data.model.Wishlist
import com.example.wishlistapp.data.repository.WishlistRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random


class WishlistViewModel ()  : ViewModel() {


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
    fun addWishlist(title : String,description : String, isPrivate : Boolean,  eventDate :String
    ) : Int {
        val wishlistId = Random.nextInt(1000, 9999)
        val generatedLink =  "wishlistapp.com/share/${Random.nextInt(100000, 999999)}"
        val wishlist =  Wishlist(
            id = wishlistId,
            title = title,
            description = description,
            isPrivate = isPrivate,
            eventDate = LocalDate.parse(
                eventDate,
                DateTimeFormatter.ofPattern("дд.мм.гггг")
            ),
            publicLink = if (isPrivate) null else generatedLink,
            ownerName = "Вы",
            gifts = emptyList()
        )
        WishlistRepository.addWishlist(wishlist)
        return wishlistId
    }
    fun deleteWishlist(wishlistId: Int) {
        WishlistRepository.deleteWishlist(wishlistId)
    }
    fun findWishlist(publicLink: String): Wishlist? = WishlistRepository.findWishlistByLink(publicLink)

    fun getGift(giftId: Int) = WishlistRepository.getGiftById(giftId)
    fun addGift(gift: Gift) {
        WishlistRepository.addGift(gift)
    }
    fun getAllGifts(): List<Gift> = WishlistRepository.getAllGifts()
    fun deleteGift(giftId: Int) {
        WishlistRepository.deleteGift(giftId)
    }
    fun getGiftById(giftId: Int): Gift? = WishlistRepository.getGiftById(giftId)
    fun cancelReservation(giftId: Int) {
        WishlistRepository.cancelReservation(giftId)
    }
    fun getGiftsByWishlistId(wishlistId: Int): List<Gift> = WishlistRepository.getGiftsByWishlistId(wishlistId)

}
