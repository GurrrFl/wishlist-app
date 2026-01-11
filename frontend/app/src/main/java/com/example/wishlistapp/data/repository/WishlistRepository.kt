package com.example.wishlistapp.data.repository


import androidx.compose.runtime.mutableStateListOf
import com.example.wishlistapp.data.model.Gift
import com.example.wishlistapp.data.model.GiftStatus
import com.example.wishlistapp.data.model.Wishlist
import java.time.LocalDate

object WishlistRepository {

    private val wishlists = mutableStateListOf(
        Wishlist(
            id = 1,
            title = "День Рождения!",
            ownerName = "Aleksandra Petrova",
            eventDate = LocalDate.of(2025, 12, 15),
            description = "Мой день рождения! Буду рада любому подарку из списка. Предпочитаю практичные вещи и гаджеты, увлекаюсь кклассической литературой 19го века. 🎁🎁",
            isPrivate = false,
            publicLink = "wishlist.app/53291",
            gifts = listOf(
                Gift(
                    id = 1,
                    name = "AirPods или похожие",
                    price = "4 990 ₽",
                    description = "Наушники желательно с шумоподавлением"
                ),
                Gift(
                    id = 2,
                    name = "Книга о любви",
                    price = "1 990 ₽",
                    description = "Классическая литература",
                    status = GiftStatus.RESERVED,
                    reservedBy = "kate_rosan"
                ),
                Gift(
                    id = 3,
                    name = "Чайник заварочный с фильтром",
                    price = "590 ₽",
                    description = "Желательно не стеклянный, и со съемным фильтром",
                    created = LocalDate.of(2025, 10, 6),

                ),
                Gift(
                    id = 4,
                    name = "Книга \"Грозовой перевал\"",
                    price = "990 ₽",
                    description = "Классическая литература от издательства Росмен",
                    status = GiftStatus.RESERVED,
                    reservedBy = "kate_rosan",
                    created = LocalDate.of(2025, 11, 3),
                ),
            )
        )
    )

    fun getWishlists(): List<Wishlist> = wishlists

    fun getWishlistById(id: Int): Wishlist? =
        wishlists.find { it.id == id }

    fun reserveGift(wishlistId: Int, giftId: Int, userName: String) {
        val wishlistIndex = wishlists.indexOfFirst { it.id == wishlistId }
        if (wishlistIndex == -1) return

        val wishlist = wishlists[wishlistIndex]
        val updatedGifts = wishlist.gifts.map {
            if (it.id == giftId && it.status == GiftStatus.AVAILABLE)
                it.copy(status = GiftStatus.RESERVED, reservedBy = userName)
            else it
        }

        wishlists[wishlistIndex] = wishlist.copy(gifts = updatedGifts)
    }
    fun addWishlist(wishlist: Wishlist) {
        wishlists.add(wishlist)
    }
    fun deleteWishlist(wishlistId: Int) {
        wishlists.removeIf { it.id == wishlistId }
    }
}
