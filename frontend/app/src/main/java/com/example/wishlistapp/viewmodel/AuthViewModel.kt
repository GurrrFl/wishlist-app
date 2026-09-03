package com.example.wishlistapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlistapp.data.repository.AuthRepository
import com.example.wishlistapp.data.remote.toAppError
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.ui.screens.auth.AuthState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var state by mutableStateOf<AuthState>(AuthState.Idle); private set
    var isLoggedIn by mutableStateOf(repository.isLoggedIn()); private set
    fun checkAuthState() { isLoggedIn = repository.isLoggedIn() }
    fun logout() = viewModelScope.launch { repository.logout(); isLoggedIn = false; state = AuthState.Success }
    fun login(email: String, password: String) {
        if (state == AuthState.Loading) return
        if (email.isBlank() || password.isBlank()) { state = AuthState.LoginError("Заполните все поля"); return }
        state = AuthState.Loading
        viewModelScope.launch {
            try {
                repository.login(email, password)
                isLoggedIn = repository.isLoggedIn()
                state = AuthState.Success
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val mapped = error.toAppError()
                state = AuthState.LoginError(
                    if (mapped == AppError.Unauthorized) "Неверный логин или пароль" else mapped.toUserMessage()
                )
            }
        }
    }
    fun register(login: String, email: String, password: String, repeatPassword: String) {
        if (state == AuthState.Loading) return
        if (login.isBlank() || email.isBlank() || password.isBlank() || repeatPassword.isBlank()) { state = AuthState.RegisterError("Заполните все поля"); return }
        if (password != repeatPassword) { state = AuthState.RegisterError("Пароли не совпадают"); return }
        state = AuthState.Loading
        viewModelScope.launch {
            try {
                repository.register(login, email, password)
                isLoggedIn = repository.isLoggedIn()
                state = AuthState.Success
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val mapped = error.toAppError()
                state = AuthState.RegisterError(
                    if (mapped == AppError.Conflict) "Пользователь уже существует" else mapped.toUserMessage()
                )
            }
        }
    }
    fun resetState() { state = AuthState.Idle }
}
