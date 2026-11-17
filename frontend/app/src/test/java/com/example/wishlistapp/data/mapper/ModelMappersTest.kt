package com.example.wishlistapp.data.mapper

import com.example.wishlistapp.data.model.dto.GiftDto
import com.example.wishlistapp.data.model.dto.ReservationDto
import com.example.wishlistapp.data.model.dto.WishlistDto
import com.example.wishlistapp.domain.model.GiftStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

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

    @Test fun mapsReservationTimestampWithOrWithoutOffset() {
        val local = ReservationDto(4, 7, 2, "2026-07-19T12:38:00", null)
        val offset = local.copy(reservedDate = "2026-07-19T12:38:00+03:00")

        assertEquals(LocalDateTime.of(2026, 7, 19, 12, 38), local.toDomain().reservedAt)
        assertEquals(local.toDomain().reservedAt, offset.toDomain().reservedAt)
    }

    @Test fun mapsNullableGiftFieldsAndMissingGiftCollection() {
        val wishlist = WishlistDto(3, 9, "Birthday", "2026-05-01", true, null, "2026-04-01T12:00:00", null)
        assertEquals(emptyList<Any>(), wishlist.toDomain().gifts)

        val gift = GiftDto(7, 3, "Book", null, null, null, "AVAILABLE", null).toDomain()
        assertEquals(GiftStatus.AVAILABLE, gift.status)
        assertNull(gift.price)
        assertNull(gift.description)
        assertNull(gift.link)
        assertNull(gift.createdAt)
    }

    @Test fun rejectsUnknownStatusAndMalformedDates() {
        assertThrows(InvalidDtoException::class.java) {
            GiftDto(7, 3, "Book", null, null, null, "gifted", null).toDomain()
        }
        assertThrows(java.time.format.DateTimeParseException::class.java) {
            WishlistDto(3, 9, "Birthday", "tomorrow", false, null, "2026-04-01", null).toDomain()
        }
        assertThrows(java.time.format.DateTimeParseException::class.java) {
            ReservationDto(4, 7, 2, "not-a-date", null).toDomain()
        }
    }
}
