package com.example.wishlistapp.data.remote

import com.example.wishlistapp.data.model.dto.ReservationDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReservationApi {
    @POST("reservations/gift/{giftId}") suspend fun reserve(@Path("giftId") giftId: Int): ReservationDto
    @GET("reservations/my") suspend fun getMine(@Query("only_active") onlyActive: Boolean = true): List<ReservationDto>
    @POST("reservations/{id}/cancel") suspend fun cancel(@Path("id") id: Int): ReservationDto
}
