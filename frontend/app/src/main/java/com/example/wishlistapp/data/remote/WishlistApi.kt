package com.example.wishlistapp.data.remote

import com.example.wishlistapp.data.model.dto.CreateWishlistDto
import com.example.wishlistapp.data.model.dto.UpdateWishlistDto
import com.example.wishlistapp.data.model.dto.WishlistDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface WishlistApi {
    @GET("wishlists/{id}") suspend fun getWishlist(@Path("id") id: Int): WishlistDto
    @GET("wishlists/link/{link}") suspend fun findByPublicLink(@Path("link") link: String): WishlistDto
    @POST("wishlists/") suspend fun create(@Body request: CreateWishlistDto): WishlistDto
    @PATCH("wishlists/{id}") suspend fun update(@Path("id") id: Int, @Body request: UpdateWishlistDto): WishlistDto
    @POST("wishlists/{id}/regenerate-link") suspend fun regenerateLink(@Path("id") id: Int): WishlistDto
    @DELETE("wishlists/{id}") suspend fun delete(@Path("id") id: Int)
}
