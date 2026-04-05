package com.example.wishlistapp.data.model.dto

import com.google.gson.annotations.SerializedName

data class ReservationDto(
    @SerializedName("reservation_id") val reservationId: Int,
    @SerializedName("gift_id") val giftId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("reserved_date") val reservedDate: String,
    @SerializedName("cancelled_at") val cancelledAt: String?
)
