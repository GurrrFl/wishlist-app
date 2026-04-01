package com.example.wishlistapp.data.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("email")
    val email: String,

)

