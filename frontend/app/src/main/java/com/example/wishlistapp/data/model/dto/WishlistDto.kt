package com.example.wishlistapp.data.model.dto

import com.google.gson.annotations.SerializedName

data class WishlistDto(
    @SerializedName("wishlist_id") val wishlistId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("event_date") val eventDate: String,
    @SerializedName("is_private") val isPrivate: Boolean,
    @SerializedName("unique_link") val uniqueLink: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("gifts") val gifts: List<GiftDto> = emptyList()
)

data class CreateWishlistDto(
    @SerializedName("name") val name: String,
    @SerializedName("event_date") val eventDate: String,
    @SerializedName("is_private") val isPrivate: Boolean,
    @SerializedName("user_id") val userId: Int
)

data class UpdateWishlistDto(
    @SerializedName("name") val name: String?,
    @SerializedName("event_date") val eventDate: String?,
    @SerializedName("is_private") val isPrivate: Boolean?
)
