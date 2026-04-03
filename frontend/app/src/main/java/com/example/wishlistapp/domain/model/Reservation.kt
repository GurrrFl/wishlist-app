package com.example.wishlistapp.domain.model

data class Reservation(
    val id: Int,
    val giftId: Int,
    val userId: Int,
    val reservedBy: String? = null
)
