package com.example.wishlistapp.domain.model

import java.time.LocalDateTime

data class Reservation(
    val id: Int,
    val giftId: Int,
    val userId: Int,
    val reservedBy: String? = null,
    val reservedAt: LocalDateTime? = null
)
