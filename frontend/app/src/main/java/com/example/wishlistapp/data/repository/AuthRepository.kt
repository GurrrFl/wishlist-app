package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.model.RegisterRequest
import com.example.wishlistapp.data.remote.AuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        val response = api.login(email, password)

        if (response.accessToken.isBlank()) {
            error("Empty access token")
        }
        try {
            sessionManager.saveToken(response.accessToken)
            val user = api.getMe()
            sessionManager.saveUser(user.userId, user.login)
        } catch (error: Exception) {
            sessionManager.clear()
            throw error
        }
    }


    suspend fun register(login: String, email: String, password: String) = withContext(Dispatchers.IO) {
        api.register(RegisterRequest(login, email, password))
    }

    suspend fun logout() = withContext(Dispatchers.IO) { sessionManager.clear() }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}
