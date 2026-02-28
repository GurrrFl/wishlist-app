package com.example.wishlistapp.data.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("login")
    val login: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("user_id")
    val user_id: Int,
)
