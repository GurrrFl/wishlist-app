package com.example.wishlistapp.data.repository

import android.util.Log
import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.model.RegisterRequest
import com.example.wishlistapp.data.remote.UserApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: UserApi,
    private val sessionManager: SessionManager
) {

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        val response = api.login(email, password)

        if (response.accessToken.isBlank()) {
            error("Empty access token")
        }
        Log.d("AuthRepository", "Token: ${response.accessToken}")
        sessionManager.saveToken(response.accessToken)
    }


    suspend fun register(login: String, email: String, password: String) = withContext(Dispatchers.IO) {
        val response = api.register(
            RegisterRequest(login, email, password))
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken()
            if (!token.isNullOrBlank()) {
                api.logout("Bearer $token")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Logout error", e)
        } finally {
            sessionManager.clear()
        }
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}
