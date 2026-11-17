package com.example.wishlistapp.data.model.dto

import com.google.gson.annotations.SerializedName

data class GiftDto(
    @SerializedName("gift_id") val giftId: Int,
    @SerializedName("wishlist_id") val wishlistId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double?,
    @SerializedName("description") val description: String?,
    @SerializedName("store_link") val storeLink: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class CreateGiftDto(
    @SerializedName("wishlist_id") val wishlistId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double?,
    @SerializedName("description") val description: String?,
    @SerializedName("store_link") val storeLink: String?
)

data class UpdateGiftDto(
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("description") val description: String?,
    @SerializedName("store_link") val storeLink: String?
)
