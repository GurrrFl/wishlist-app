package com.example.wishlistapp.domain.model

import java.time.LocalDate

data class Wishlist(
    val id: Int,
    val title: String,
    val ownerName: String,
    val eventDate: LocalDate,
    val description: String? = null,
    val isPrivate: Boolean,
    val publicLink: String?,
    val gifts: List<Gift> = emptyList()
)
