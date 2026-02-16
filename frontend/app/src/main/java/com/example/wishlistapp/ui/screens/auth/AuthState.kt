package com.example.wishlistapp.ui.screens.auth

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()

    data class LoginError(val message: String) : AuthState()
    data class RegisterError(val message: String) : AuthState()
    object Success : AuthState()
}
