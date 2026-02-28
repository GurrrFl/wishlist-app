package com.example.wishlistapp.data.model

import java.time.LocalDate

data class Wishlist(
    val id: Int,
    val title: String,
    val ownerName: String,
    val eventDate: LocalDate,
    val description: String,
    val isPrivate: Boolean,
    val publicLink: String?,
    val gifts: List<Gift>
)
