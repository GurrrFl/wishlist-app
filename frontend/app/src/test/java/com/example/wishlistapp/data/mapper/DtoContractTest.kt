package com.example.wishlistapp.data.mapper

import com.example.wishlistapp.data.model.dto.CreateGiftDto
import com.example.wishlistapp.data.model.dto.CreateWishlistDto
import com.example.wishlistapp.data.model.dto.GiftDto
import com.example.wishlistapp.data.model.dto.ReservationDto
import com.example.wishlistapp.data.model.dto.WishlistDto
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DtoContractTest {
    private val gson = Gson()

    @Test fun outgoingWishlistUsesBackendNamesOnly() {
        val json = JsonParser.parseString(gson.toJson(CreateWishlistDto("Books", "2030-01-01", false, 7))).asJsonObject
        assertEquals("Books", json.get("name").asString)
        assertEquals(7, json.get("user_id").asInt)
        assertTrue(json.has("event_date"))
        assertFalse(json.has("title"))
        assertFalse(json.has("wishlist_id"))
        assertFalse(json.has("created_at"))
    }

    @Test fun outgoingGiftUsesBackendNamesOnly() {
        val json = JsonParser.parseString(gson.toJson(CreateGiftDto(4, "Book", 12.5, null, "https://example.org"))).asJsonObject
        assertEquals(4, json.get("wishlist_id").asInt)
        assertEquals("https://example.org", json.get("store_link").asString)
        assertFalse(json.has("gift_id"))
        assertFalse(json.has("status"))
        assertFalse(json.has("created_at"))
    }

    @Test fun incomingBackendDtosMapToDomainWithoutExposingDtoTypes() {
        val wishlist = gson.fromJson("""{"wishlist_id":4,"user_id":7,"name":"Books","event_date":"2030-01-01","is_private":false,"unique_link":"abc","created_at":"2029-12-01","gifts":[]}""", WishlistDto::class.java)
        val gift = gson.fromJson("""{"gift_id":9,"wishlist_id":4,"name":"Book","price":12.5,"description":null,"store_link":null,"status":"reserved","created_at":"2029-12-01T12:00:00"}""", GiftDto::class.java)
        val reservation = gson.fromJson("""{"reservation_id":5,"gift_id":9,"user_id":3,"reserved_date":"2029-12-02T12:00:00","cancelled_at":null}""", ReservationDto::class.java)
        assertEquals(4, wishlist.toDomain().id)
        assertEquals(9, gift.toDomain().id)
        assertEquals(5, reservation.toDomain().id)
    }
}
