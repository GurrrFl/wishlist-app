package com.example.wishlistapp.data.repository

import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.data.local.FakeSnapshot
import com.example.wishlistapp.data.local.FakeSnapshotStorage
import com.example.wishlistapp.data.local.toDomain
import com.example.wishlistapp.data.local.toStored
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime

/** Single in-memory source. Values published from it are immutable Lists. */
class FakeDataStore(private val storage: FakeSnapshotStorage? = null) {
    private val restored = storage?.load()?.let { snapshot ->
        runCatching {
            snapshot.wishlists.map { it.toDomain() } to snapshot.reservations.map { it.toDomain() }
        }.getOrNull()
    }
    private val _wishlists = MutableStateFlow<List<Wishlist>>(restored?.first ?: demoWishlists)
    val wishlists: StateFlow<List<Wishlist>> = _wishlists.asStateFlow()
    private val _reservations = MutableStateFlow<List<Reservation>>(restored?.second ?: listOf(
        Reservation(1, 2, 2, "kate_rosan"), Reservation(2, 16, 2, "kate_rosan"), Reservation(3, 18, 3, "Иван Иванов")
    ))
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()
    var nextWishlistId = (_wishlists.value.maxOfOrNull { it.id } ?: 0) + 1
    var nextGiftId = (_wishlists.value.flatMap { it.gifts }.maxOfOrNull { it.id } ?: 0) + 1
    var nextReservationId = (_reservations.value.maxOfOrNull { it.id } ?: 0) + 1

    fun updateWishlists(transform: (List<Wishlist>) -> List<Wishlist>) {
        _wishlists.value = transform(_wishlists.value).toList()
        persist()
    }
    fun updateReservations(transform: (List<Reservation>) -> List<Reservation>) {
        _reservations.value = transform(_reservations.value).toList()
        persist()
    }
    private fun persist() {
        storage?.save(FakeSnapshot(
            _wishlists.value.map { it.toStored() },
            _reservations.value.map { it.toStored() }
        ))
    }

    private companion object {
        val demoWishlists = listOf(
            Wishlist(1, "День Рождения!", 1, "Aleksandra Petrova", LocalDate.of(2025, 12, 15), "Мой день рождения! Буду рада любому подарку из списка.", false, "wishlist.app/53291", listOf(
                gift(1, 1, "AirPods или похожие", 4990.0), gift(2, 1, "Книга о любви", 1990.0, GiftStatus.RESERVED, "kate_rosan"), gift(3, 1, "Чайник заварочный SKYPHOS", 1990.0), gift(4, 1, "Книга Грозовой перевал", 990.0), gift(5, 1, "Набор ароматических свечей", 1490.0)
            )),
            Wishlist(2, "Годовщина 💕", 1, "Aleksandra Petrova", LocalDate.of(2025, 7, 20), "Наша годовщина!", true, null, listOf(
                gift(6, 2, "Браслеты MineLife", 2490.0), gift(7, 2, "Кружка CupCraft", 890.0), gift(8, 2, "Карманные часы", 3990.0), gift(9, 2, "Настольная игра", 2590.0)
            )),
            Wishlist(3, "8 Марта!", 1, "Aleksandra Petrova", LocalDate.of(2026, 12, 31), "Праздничные подарки", false, "wishlist.app/98342", listOf(
                gift(10, 3, "Пазл Hatber 1000 элементов", 1290.0), gift(11, 3, "Набор для рисования", 2190.0), gift(12, 3, "Шоколад Lindt", 1590.0), gift(13, 3, "Набор для вышивки", 1790.0), gift(14, 3, "Канва Малевич", 990.0)
            )),
            Wishlist(4, "Новый год! 🎄✨", 2, "kate_rosan", LocalDate.of(2025, 12, 31), "Новогодние подарки", false, "wishlist.app/56739", listOf(
                gift(20, 4, "Пазл Hatber 1000 элементов", 1290.0), gift(15, 4, "Набор для рисования", 2190.0), gift(16, 4, "Шоколад Lindt", 1590.0, GiftStatus.RESERVED, "kate_rosan"), gift(17, 4, "Набор для вышивки", 1790.0), gift(18, 4, "Канва Малевич", 990.0, GiftStatus.RESERVED, "Иван Иванов")
            ))
        )
        fun gift(id: Int, wishlistId: Int, name: String, price: Double, status: GiftStatus = GiftStatus.AVAILABLE, reservedBy: String? = null) = Gift(id, wishlistId, name, price, "Тестовое описание подарка", null, status, LocalDateTime.of(2025, 10, 1, 12, 0), if (wishlistId == 4) "kate_rosan" else "Aleksandra Petrova", reservedBy)
    }
}
