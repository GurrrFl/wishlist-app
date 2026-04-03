package com.example.wishlistapp.domain.model

import java.time.LocalDate

data class Wishlist(
    val id: Int,
    val title: String,
    val ownerId: Int,
    /** Display-only data; FastAPI WishlistRead does not include it. */
    val ownerName: String? = null,
    val eventDate: LocalDate,
    val description: String? = null,
    val isPrivate: Boolean,
    val publicLink: String?,
    val gifts: List<Gift> = emptyList()
)
