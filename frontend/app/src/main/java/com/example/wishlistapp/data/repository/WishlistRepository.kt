package com.example.wishlistapp.data.repository


import androidx.compose.runtime.mutableStateListOf
import com.example.wishlistapp.data.model.Gift
import com.example.wishlistapp.data.model.GiftStatus
import com.example.wishlistapp.data.model.Wishlist
import java.time.LocalDate

object  WishlistRepository  {

     fun getWishlists(): List<Wishlist> = wishlists

     fun getWishlistById(id: Int): Wishlist? =
        wishlists.find { it.id == id }
     fun getAllGifts(): List<Gift> = wishlists.flatMap { it.gifts }

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
     fun findWishlistByLink(link: String): Wishlist? = wishlists.find { it.publicLink == link }
     fun getGiftsByWishlistId(wishlistId: Int): List<Gift> =
        wishlists.find { it.id == wishlistId }?.gifts ?: emptyList()

     fun getGiftById(giftId: Int): Gift? =
        wishlists.flatMap { it.gifts }.find { it.id == giftId }

     fun addGift(gift: Gift) {
        wishlists.find { it.id == gift.wishlistId }?.let { wishlist ->
            val updatedGifts = wishlist.gifts.toMutableList().apply {
                add(gift)
            }
            val wishlistIndex = wishlists.indexOf(wishlist)
            wishlists[wishlistIndex] = wishlist.copy(gifts = updatedGifts)
        }
    }

     fun deleteGift(giftId: Int) {
        val giftIndex = wishlists.flatMap { it.gifts }.indexOfFirst { it.id == giftId }
        if (giftIndex != -1) {
            val wishlistId = wishlists.flatMap { it.gifts }
                .indexOfFirst { it.id == giftId }
                .let { index ->
                    wishlists.flatMap { it.gifts }[index].wishlistId
                }

            wishlists.find { it.id == wishlistId }?.let { wishlist ->
                val updatedGifts = wishlist.gifts.filter { it.id != giftId }
                val wishlistIndex = wishlists.indexOf(wishlist)
                wishlists[wishlistIndex] = wishlist.copy(gifts = updatedGifts)
            }
        }
    }

     fun cancelReservation(giftId: Int) {
        wishlists.forEachIndexed { wishlistIndex, wishlist ->
            val giftIndex = wishlist.gifts.indexOfFirst { it.id == giftId }
            if (giftIndex != -1) {
                val updatedGifts = wishlist.gifts.toMutableList().apply {
                    this[giftIndex] = this[giftIndex].copy(
                        status = GiftStatus.AVAILABLE,
                        reservedBy = null
                    )
                }
                wishlists[wishlistIndex] = wishlist.copy(gifts = updatedGifts)
            }
        }
    }
    //
    private val wishlists = mutableStateListOf(
        Wishlist(
            id = 1,
            title = "День Рождения!",
            ownerName = "Aleksandra Petrova",
            eventDate = LocalDate.of(2025, 12, 15),
            description = "Мой день рождения! Буду рада любому подарку из списка. Предпочитаю практичные вещи и гаджеты, увлекаюсь классической литературой 19го века. 🎁🎁",
            isPrivate = false,
            publicLink = "wishlist.app/53291",
            gifts = listOf(
                Gift(
                    id = 1,
                    wishlistId = 1,
                    name = "AirPods или похожие",
                    price = "4 990 ₽",
                    description = "Наушники желательно с шумоподавлением",
                    link = "https://www.wildberries.ru/catalog/123456789/detail.aspx",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 10, 1),
                    ownerName = "Aleksandra Petrova"
                ),
                Gift(
                    id = 2,
                    wishlistId = 1,
                    name = "Книга о любви",
                    price = "1 990 ₽",
                    description = "Классическая литература",
                    link = "https://www.wildberries.ru/catalog/143592972/detail.aspx?size=242472035",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 10, 5),
                    ownerName = "Aleksandra Petrova",
                    reservedBy = "kate_rosan"
                ),
                Gift(
                    id = 3,
                    wishlistId = 1,
                    name = "Чайник заварочный SKYPHOS",
                    price = "1 990 ₽",
                    description = "Керамический чайник со съемным фильтром, идеален для заваривания травяных чаев",
                    link = "https://www.wildberries.ru/catalog/111109236/detail.aspx?size=201414280",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 10, 6),
                    ownerName = "Aleksandra Petrova"
                ),
                Gift(
                    id = 4,
                    wishlistId = 1,
                    name = "Книга \"Грозовой перевал\" Эксмо",
                    price = "990 ₽",
                    description = "Классическая литература от издательства Эксмо в красивом оформлении",
                    link = "https://www.wildberries.ru/catalog/143592972/detail.aspx?size=242472035",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 11, 3),
                    ownerName = "Aleksandra Petrova",
                    reservedBy = "kate_rosan"
                ),
                Gift(
                    id = 5,
                    wishlistId = 1,
                    name = "Набор ароматических свечей Paragraph Collection",
                    price = "1 490 ₽",
                    description = "Красивые декоративные свечи с разными ароматами для уюта",
                    link = "https://www.wildberries.ru/catalog/217899612/detail.aspx?size=347009447",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 11, 10),
                    ownerName = "Aleksandra Petrova"
                )
            )
        ),
        Wishlist(
            id = 2,
            title = "Годовщина 💕",
            ownerName = "Aleksandra Petrova",
            eventDate = LocalDate.of(2025, 7, 20),
            description = "Наша годовщина! Хочу что-то романтичное и памятное. Возможно, настольные игры или уютные вещи для дома.",
            isPrivate = true,
            publicLink = "wishlist.app/74291",
            gifts = listOf(
                Gift(
                    id = 6,
                    wishlistId = 2,
                    name = "Браслеты MineLife",
                    price = "2 490 ₽",
                    description = "Стильные браслеты с гравировкой, отличный подарок на память",
                    link = "https://www.wildberries.ru/catalog/259768946/detail.aspx?size=403890636",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 6, 15),
                    ownerName = "Aleksandra Petrova"
                ),
                Gift(
                    id = 7,
                    wishlistId = 2,
                    name = "Кружка CupCraft с дизайном",
                    price = "890 ₽",
                    description = "Керамическая кружка ручной росписи, идеальна для чая по вечерам",
                    link = "https://www.wildberries.ru/catalog/324335042/detail.aspx?size=487195946",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 6, 20),
                    ownerName = "Aleksandra Petrova"
                ),
                Gift(
                    id = 8,
                    wishlistId = 2,
                    name = "Карманные часы Time Lider",
                    price = "3 990 ₽",
                    description = "Классические механические часы в ретро стиле",
                    link = "https://www.wildberries.ru/catalog/486116606/detail.aspx?size=677923636",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 6, 25),
                    ownerName = "Aleksandra Petrova"
                ),
                Gift(
                    id = 9,
                    wishlistId = 2,
                    name = "Настольная игра ",
                    price = "2 590 ₽",
                    description = "Интересная стратегическая игра для уютных вечеров вдвоем",
                    link = "https://www.wildberries.ru/catalog/232553104/detail.aspx?size=366819059",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 7, 1),
                    ownerName = "Aleksandra Petrova"
                )
            )
        ),
        Wishlist(
            id = 3,
            title = "8 Марта!",
            ownerName = "Aleksandra Petrova",
            eventDate = LocalDate.of(2026, 12, 31),
            description = "Новогодние подарки! Хочу творческие наборы, головоломки и сладости для праздничного настроения 🎅",
            isPrivate = false,
            publicLink = "wishlist.app/98342",
            gifts = listOf(
                Gift(
                    id = 10,
                    wishlistId = 3,
                    name = "Пазл Hatber 1000 элементов",
                    price = "1 290 ₽",
                    description = "Красивый пазл для долгих зимних вечеров",
                    link = "https://www.wildberries.ru/catalog/260452179/detail.aspx?size=404859863",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 11, 15),
                    ownerName = "Aleksandra Petrova"
                ),
                Gift(
                    id = 11,
                    wishlistId = 3,
                    name = "Набор для рисования по номерам",
                    price = "2 190 ₽",
                    description = "Картина по номерам с кистями и красками",
                    link = "https://www.wildberries.ru/catalog/237988113/detail.aspx?size=374504646",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 11, 20),
                    ownerName = "Aleksandra Petrova"
                ),
                Gift(
                    id = 12,
                    wishlistId = 3,
                    name = "Шоколад Lindt премиум",
                    price = "1 590 ₽",
                    description = "Набор изысканных шоколадных конфет в подарочной упаковке",
                    link = "https://www.wildberries.ru/catalog/321921587/detail.aspx?size=484412787",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 11, 25),
                    ownerName = "Aleksandra Petrova"
                ),
                Gift(
                    id = 13,
                    wishlistId = 3,
                    name = "Набор для вышивки \"Мир Вышивки\"",
                    price = "1 790 ₽",
                    description = "Полный комплект для вышивки крестом с красивым дизайном",
                    link = "https://www.wildberries.ru/catalog/267640834/detail.aspx?size=414622313",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 12, 1),
                    ownerName = "Aleksandra Petrova"
                ),
                Gift(
                    id = 14,
                    wishlistId = 3,
                    name = "Канва \"Малевичъ\" для вышивки",
                    price = "990 ₽",
                    description = "Качественная канва с нанесенным рисунком для творчества",
                    link = "https://www.wildberries.ru/catalog/82464813/detail.aspx?size=135901932",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 12, 5),
                    ownerName = "Aleksandra Petrova"
                )
            )
        ),
        Wishlist(
            id = 4,
            title = "Новый год! 🎄✨",
            ownerName = "kate_rosan",
            eventDate = LocalDate.of(2025, 12, 31),
            description = "Новогодние подарки! Хочу творческие наборы, головоломки и сладости для праздничного настроения 🎅",
            isPrivate = false,
            publicLink = "wishlist.app/56739",
            gifts = listOf(
                Gift(
                    id = 20,
                    wishlistId = 4,
                    name = "Пазл Hatber 1000 элементов",
                    price = "1 290 ₽",
                    description = "Красивый пазл для долгих зимних вечеров",
                    link = "https://www.wildberries.ru/catalog/260452179/detail.aspx?size=404859863",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 11, 15),
                    ownerName = "kate_rosan"
                ),
                Gift(
                    id = 15,
                    wishlistId = 4,
                    name = "Набор для рисования по номерам",
                    price = "2 190 ₽",
                    description = "Картина по номерам с кистями и красками",
                    link = "https://www.wildberries.ru/catalog/237988113/detail.aspx?size=374504646",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 11, 20),
                    ownerName = "kate_rosan"
                ),
                Gift(
                    id = 16,
                    wishlistId = 4,
                    name = "Шоколад Lindt премиум",
                    price = "1 590 ₽",
                    description = "Набор изысканных шоколадных конфет в подарочной упаковке",
                    link = "https://www.wildberries.ru/catalog/321921587/detail.aspx?size=484412787",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 11, 25),
                    ownerName = "kate_rosan"
                ),
                Gift(
                    id = 17,
                    wishlistId = 4,
                    name = "Набор для вышивки \"Мир Вышивки\"",
                    price = "1 790 ₽",
                    description = "Полный комплект для вышивки крестом с красивым дизайном",
                    link = "https://www.wildberries.ru/catalog/267640834/detail.aspx?size=414622313",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 12, 1),
                    ownerName = "kate_rosan"
                ),
                Gift(
                    id = 18,
                    wishlistId = 4,
                    name = "Канва \"Малевичъ\" для вышивки",
                    price = "990 ₽",
                    description = "Качественная канва с нанесенным рисунком для творчества",
                    link = "https://www.wildberries.ru/catalog/82464813/detail.aspx?size=135901932",
                    status = GiftStatus.AVAILABLE,
                    created = LocalDate.of(2025, 12, 5),
                    reservedBy = "Иван Иванов",
                    ownerName = "kate_rosan"
                )
            )
        )

    )


}
