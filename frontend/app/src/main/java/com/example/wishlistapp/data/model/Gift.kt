package com.example.wishlistapp.data.model

import java.time.LocalDate


enum class GiftStatus {
    AVAILABLE,
    RESERVED
}

data class Gift(
    val id: Int,
    val wishlistId: Int,
    val name: String,
    val price: String,
    val description: String,
    val link: String?,
    val status: GiftStatus = GiftStatus.AVAILABLE,
    val created: LocalDate,
    val ownerName: String,
    val reservedBy: String? = null
)
