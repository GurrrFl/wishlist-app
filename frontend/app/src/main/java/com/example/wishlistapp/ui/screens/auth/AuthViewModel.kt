package com.example.wishlistapp.ui.screens.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlistapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            state = AuthState.Error("Заполните все поля")
            return
        }

        viewModelScope.launch {
            state = AuthState.Loading
            try {
                repository.login(email, password)
                state = AuthState.Success
            } catch (e: Exception) {
                Log.e("AUTH", "Login error", e)
                state = AuthState.Error(e.message ?: "Ошибка")
                state = AuthState.Error("Ошибка входа")
            }
        }
    }

    fun register(login: String, email: String, password: String, repeatPassword: String) {
        if (login.isBlank() || email.isBlank() || password.isBlank() || repeatPassword.isBlank()) {
            state = AuthState.Error("Заполните все поля")
            return
        }

        if (password != repeatPassword) {
            state = AuthState.Error("Пароли не совпадают")
            return
        }

        viewModelScope.launch {
            state = AuthState.Loading
            try {
                repository.register(login, email, password)
                state = AuthState.Success
            } catch (e: Exception) {
                Log.e("AUTH", "Login error", e)
                state = AuthState.Error(e.message ?: "Ошибка регистрации")
                state = AuthState.Error("Ошибка регистрации")
            }
        }
    }

    fun resetState() {
        state = AuthState.Idle
    }
}

