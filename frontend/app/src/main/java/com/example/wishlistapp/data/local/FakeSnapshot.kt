package com.example.wishlistapp.data.local

import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.model.Wishlist
import java.time.LocalDate
import java.time.LocalDateTime

data class FakeSnapshot(val wishlists: List<StoredWishlist>, val reservations: List<StoredReservation>)

data class StoredReservation(
    val id: Int, val giftId: Int, val userId: Int,
    val reservedBy: String?, val reservedAt: String?
)

data class StoredWishlist(
    val id: Int, val title: String, val ownerId: Int, val ownerName: String?,
    val eventDate: String, val description: String?, val isPrivate: Boolean,
    val publicLink: String?, val gifts: List<StoredGift>
)

data class StoredGift(
    val id: Int, val wishlistId: Int, val name: String, val price: Double?,
    val description: String?, val link: String?, val status: String,
    val createdAt: String?, val ownerName: String?, val reservedBy: String?
)

fun Wishlist.toStored() = StoredWishlist(
    id, title, ownerId, ownerName, eventDate.toString(), description, isPrivate,
    publicLink, gifts.map(Gift::toStored)
)

fun StoredWishlist.toDomain() = Wishlist(
    id, title, ownerId, ownerName, LocalDate.parse(eventDate), description,
    isPrivate, publicLink, gifts.map(StoredGift::toDomain)
)

fun Gift.toStored() = StoredGift(
    id, wishlistId, name, price, description, link, status.name,
    createdAt?.toString(), ownerName, reservedBy
)

fun StoredGift.toDomain() = Gift(
    id, wishlistId, name, price, description, link, GiftStatus.valueOf(status),
    createdAt?.let(LocalDateTime::parse), ownerName, reservedBy
)

fun Reservation.toStored() = StoredReservation(id, giftId, userId, reservedBy, reservedAt?.toString())

fun StoredReservation.toDomain() = Reservation(
    id, giftId, userId, reservedBy, reservedAt?.let(LocalDateTime::parse)
)
