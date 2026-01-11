package com.example.wishlistapp.data.model

import java.time.LocalDate


enum class GiftStatus {
    AVAILABLE,
    RESERVED
}

data class Gift(
    val id: Int,
    val name: String,
    val price: String,
    val description: String,
    val status: GiftStatus = GiftStatus.AVAILABLE,
    val created: LocalDate = LocalDate.of(2025, 11, 8),
    val reservedBy: String? = null
)
