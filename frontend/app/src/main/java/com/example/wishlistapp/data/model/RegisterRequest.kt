package com.example.wishlistapp.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("login")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)



