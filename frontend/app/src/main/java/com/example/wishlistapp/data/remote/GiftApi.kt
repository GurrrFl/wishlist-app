package com.example.wishlistapp.data.remote

import com.example.wishlistapp.data.model.dto.CreateGiftDto
import com.example.wishlistapp.data.model.dto.GiftDto
import com.example.wishlistapp.data.model.dto.UpdateGiftDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface GiftApi {
    @GET("gifts/wishlist/{wishlistId}") suspend fun getByWishlist(@Path("wishlistId") wishlistId: Int): List<GiftDto>
    @GET("gifts/{id}") suspend fun getGift(@Path("id") id: Int): GiftDto
    @POST("gifts/") suspend fun create(@Body request: CreateGiftDto): GiftDto
    @PATCH("gifts/{id}") suspend fun update(@Path("id") id: Int, @Body request: UpdateGiftDto): GiftDto
    @DELETE("gifts/{id}") suspend fun delete(@Path("id") id: Int)
}
