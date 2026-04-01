package com.example.wishlistapp.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlistapp.data.repository.AuthRepository
import com.example.wishlistapp.ui.screens.auth.AuthState
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var isLoggedIn by mutableStateOf(repository.isLoggedIn())
        private set

    fun checkAuthState() {
        isLoggedIn = repository.isLoggedIn()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            isLoggedIn = false
            state = AuthState.Success
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            state = AuthState.LoginError("Заполните все поля")
            return
        }

        viewModelScope.launch {
            state = AuthState.Loading
            try {
                repository.login(email, password)
                isLoggedIn = true
                state = AuthState.Success
            } catch (e: Exception) {
                Log.e("AUTH", "Login error", e)
                state = AuthState.LoginError(e.message ?: "Ошибка")
                state = AuthState.LoginError("Ошибка входа")
            }
        }
    }

    fun register(login: String, email: String, password: String, repeatPassword: String) {
        if (login.isBlank() || email.isBlank() || password.isBlank() || repeatPassword.isBlank()) {
            state = AuthState.RegisterError("Заполните все поля")
            return
        }

        if (password != repeatPassword) {
            state = AuthState.RegisterError("Пароли не совпадают")
            return
        }

        viewModelScope.launch {
            state = AuthState.Loading
            try {
                repository.register(login, email, password)
                isLoggedIn = true
                state = AuthState.Success
            } catch (e: Exception) {
                Log.e("AUTH", "Login error", e)
                state = AuthState.RegisterError(e.message ?: "Ошибка регистрации")
                state = AuthState.RegisterError("Ошибка регистрации")
            }
        }

    }

    fun resetState() {
        state = AuthState.Idle
    }
}