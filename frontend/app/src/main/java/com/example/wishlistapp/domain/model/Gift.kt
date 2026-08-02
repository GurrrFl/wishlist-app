package com.example.wishlistapp.domain.model

import java.time.LocalDateTime

enum class GiftStatus { AVAILABLE, RESERVED }

data class Gift(
    val id: Int,
    val wishlistId: Int,
    val name: String,
    val price: Double?,
    val description: String?,
    val link: String?,
    val status: GiftStatus,
    val createdAt: LocalDateTime?,
    val ownerName: String? = null,
    val reservedBy: String? = null
) {
    /** Compatibility for the existing screens; new code should use createdAt. */
    val created get() = createdAt?.toLocalDate()
}
