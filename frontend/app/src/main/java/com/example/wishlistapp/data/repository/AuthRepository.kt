package com.example.wishlistapp.data.repository

import android.util.Log
import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.data.model.RegisterRequest
import com.example.wishlistapp.data.remote.UserApi

class AuthRepository(
    private val api: UserApi,
    private val sessionManager: SessionManager
) {

    suspend fun login(email: String, password: String) {
        val response = api.login(email, password)

        if (response.accessToken.isBlank()) {
            error("Empty access token")
        }
        Log.d("AUTH", "Token: ${response.accessToken}")

        sessionManager.saveToken(response.accessToken)
    }



suspend fun register(login: String, email: String, password: String) {
    val response = api.register(
        RegisterRequest(login, email, password))}


}
