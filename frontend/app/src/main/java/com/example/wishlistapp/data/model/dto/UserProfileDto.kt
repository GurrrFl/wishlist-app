package com.example.wishlistapp.data.model.dto

import com.google.gson.annotations.SerializedName

data class UserProfileDto(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("login") val login: String,
    @SerializedName("wishlists") val wishlists: List<WishlistDto>
)
