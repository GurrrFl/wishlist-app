package com.example.wishlistapp.data.remote

import com.example.wishlistapp.data.model.RegisterRequest
import com.example.wishlistapp.data.model.TokenResponse
import com.example.wishlistapp.data.model.UserDto
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET

interface  UserApi {

    @GET("users/me")
    suspend fun getMe(): UserDto

    @POST("users/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): UserDto

    @FormUrlEncoded
    @POST("users/token")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): TokenResponse

}
