package com.example.wishlistapp.data.local

import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.data.inMemoryPreferences
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class FakeSnapshotTest {
    @Test fun roundTripPreservesDatesAndReservations() {
        val gift = Gift(4, 2, "Book", 12.5, null, null, GiftStatus.RESERVED,
            LocalDateTime.of(2026, 5, 1, 10, 30), "Owner", "Guest")
        val wishlist = Wishlist(2, "Birthday", 1, "Owner", LocalDate.of(2026, 7, 1),
            "Description", false, "abc123", listOf(gift))

        assertEquals(wishlist, wishlist.toStored().toDomain())
    }

    @Test fun persistedReservationPreservesItsDate() {
        val storage = FakeSnapshotStorage(inMemoryPreferences())
        val reservation = Reservation(7, 14, 3, "Guest", LocalDateTime.of(2026, 7, 19, 8, 56))
        storage.save(FakeSnapshot(emptyList(), listOf(reservation.toStored())))

        assertEquals(reservation, storage.load()!!.reservations.single().toDomain())
    }
}
