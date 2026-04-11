package com.example.wishlistapp.data.mapper

import com.example.wishlistapp.data.model.dto.GiftDto
import com.example.wishlistapp.data.model.dto.WishlistDto
import com.example.wishlistapp.domain.model.GiftStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ModelMappersTest {
    @Test fun mapsBackendFieldNamesAndValues() {
        val gift = GiftDto(7, 3, "Book", 42.5, null, null, "reserved", "2026-04-01T12:30:00")
        val wishlist = WishlistDto(3, 9, "Birthday", "2026-05-01", false, "abc123", "2026-04-01T12:00:00", listOf(gift))

        val result = wishlist.toDomain()

        assertEquals(3, result.id)
        assertEquals(9, result.ownerId)
        assertEquals("Birthday", result.title)
        assertEquals(LocalDate.of(2026, 5, 1), result.eventDate)
        assertEquals(GiftStatus.RESERVED, result.gifts.single().status)
        assertEquals(42.5, result.gifts.single().price!!, 0.0)
    }
}
