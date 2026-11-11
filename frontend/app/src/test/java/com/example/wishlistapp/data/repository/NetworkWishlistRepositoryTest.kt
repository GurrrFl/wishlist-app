package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.inMemoryPreferences
import com.example.wishlistapp.data.model.RegisterRequest
import com.example.wishlistapp.data.model.TokenResponse
import com.example.wishlistapp.data.model.UserDto
import com.example.wishlistapp.data.model.dto.CreateWishlistDto
import com.example.wishlistapp.data.model.dto.GiftDto
import com.example.wishlistapp.data.model.dto.UpdateWishlistDto
import com.example.wishlistapp.data.model.dto.UserProfileDto
import com.example.wishlistapp.data.model.dto.WishlistDto
import com.example.wishlistapp.data.remote.AuthApi
import com.example.wishlistapp.data.remote.WishlistApi
import com.example.wishlistapp.domain.model.GiftStatus
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkWishlistRepositoryTest {
    private val profile = object : AuthApi {
        override suspend fun register(request: RegisterRequest): UserDto = error("unused")
        override suspend fun login(login: String, password: String): TokenResponse = error("unused")
        override suspend fun getMe(): UserDto = error("unused")
        override suspend fun getProfile() = UserProfileDto(2, "guest", emptyList())
    }

    @Test fun refreshesOpenedGuestWishlistThroughPublicLink() = runBlocking {
        val session = SessionManager(inMemoryPreferences()).apply { saveUser(2, "guest") }
        var status = "available"
        var ownerOnlyCalls = 0
        val api = object : WishlistApi {
            private fun dto() = WishlistDto(4, 1, "Books", "2030-01-01", false, "abc", "2029-12-01", listOf(
                GiftDto(9, 4, "Book", 12.5, null, null, status, "2029-12-01T12:00:00")
            ))
            override suspend fun getWishlist(id: Int): WishlistDto { ownerOnlyCalls++; error("owner-only endpoint") }
            override suspend fun findByPublicLink(link: String): WishlistDto { assertEquals("abc", link); return dto() }
            override suspend fun create(request: CreateWishlistDto): WishlistDto = error("unused")
            override suspend fun update(id: Int, request: UpdateWishlistDto): WishlistDto = error("unused")
            override suspend fun regenerateLink(id: Int): WishlistDto = error("unused")
            override suspend fun delete(id: Int) = error("unused")
        }
        val repository: WishlistRepository = NetworkWishlistRepository(api, profile, session)
        assertTrue(repository.findByPublicLink("https://wishlist.app/abc") is AppResult.Success)
        status = "reserved"

        val refreshed = repository.refreshWishlist(4) as AppResult.Success
        assertEquals(GiftStatus.RESERVED, refreshed.data.gifts.single().status)
        assertEquals(GiftStatus.RESERVED, repository.observePublicWishlist().first()?.gifts?.single()?.status)
        assertEquals(0, ownerOnlyCalls)
    }
}
