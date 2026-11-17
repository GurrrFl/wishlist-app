package com.example.wishlistapp.data.mapper

import com.example.wishlistapp.data.model.dto.GiftDto
import com.example.wishlistapp.data.model.dto.ReservationDto
import com.example.wishlistapp.data.model.dto.WishlistDto
import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.model.Reservation
import com.example.wishlistapp.domain.model.Wishlist
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

class InvalidDtoException(message: String) : IllegalArgumentException(message)

fun WishlistDto.toDomain(ownerName: String? = null): Wishlist = Wishlist(
    id = wishlistId,
    title = name,
    ownerId = userId,
    ownerName = ownerName,
    eventDate = LocalDate.parse(eventDate),
    isPrivate = isPrivate,
    publicLink = uniqueLink,
    gifts = gifts.orEmpty().map(GiftDto::toDomain)
)

fun GiftDto.toDomain(): Gift = Gift(
    id = giftId,
    wishlistId = wishlistId,
    name = name,
    price = price,
    description = description,
    link = storeLink,
    status = when (status?.lowercase()) {
        "available" -> GiftStatus.AVAILABLE
        "reserved" -> GiftStatus.RESERVED
        else -> throw InvalidDtoException("Unknown gift status: $status")
    },
    createdAt = createdAt?.let { value ->
        runCatching { OffsetDateTime.parse(value).toLocalDateTime() }
            .getOrElse { LocalDateTime.parse(value) }
    }
)

fun ReservationDto.toDomain(): Reservation = Reservation(
    id = reservationId,
    giftId = giftId,
    userId = userId,
    reservedAt = try {
        OffsetDateTime.parse(reservedDate).toLocalDateTime()
    } catch (error: java.time.format.DateTimeParseException) {
        LocalDateTime.parse(reservedDate)
    }
)
