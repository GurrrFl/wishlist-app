package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.inMemoryPreferences
import com.example.wishlistapp.data.model.RegisterRequest
import com.example.wishlistapp.data.model.TokenResponse
import com.example.wishlistapp.data.model.UserDto
import com.example.wishlistapp.data.remote.AuthApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AuthRepositoryTest {
    private val user = UserDto(7, "alice", "alice@example.com")

    @Test fun loginStoresVerifiedUserAndLogoutClearsSession() = runBlocking {
        val session = SessionManager(inMemoryPreferences())
        val repository = AuthRepository(api(), session)
        repository.login("alice", "password")
        assertEquals(7, session.getUserId())
        assertTrue(repository.isLoggedIn())
        repository.logout()
        assertFalse(repository.isLoggedIn())
        assertNull(session.getUserId())
    }

    @Test fun failedProfileLookupClearsToken() = runBlocking {
        val session = SessionManager(inMemoryPreferences())
        val repository = AuthRepository(api(failProfile = true), session)
        runCatching { repository.login("alice", "password") }
        assertFalse(repository.isLoggedIn())
    }

    @Test fun registrationDoesNotCreateSession() = runBlocking {
        val session = SessionManager(inMemoryPreferences())
        AuthRepository(api(), session).register("alice", "alice@example.com", "password")
        assertFalse(session.isLoggedIn())
    }

    private fun api(failProfile: Boolean = false) = object : AuthApi {
        override suspend fun register(request: RegisterRequest) = user
        override suspend fun login(login: String, password: String) = TokenResponse("token")
        override suspend fun getMe(): UserDto {
            if (failProfile) throw IOException("offline")
            return user
        }
        override suspend fun getProfile() = error("unused")
    }
}
