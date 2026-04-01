package com.example.wishlistapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sessionManager: SessionManager
): ViewModel() {
    private val _isDarkThemeState = MutableStateFlow(sessionManager.isDarkTheme())
    val darkThemeState: StateFlow<Boolean> = _isDarkThemeState
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination


    fun toggleDarkTheme(bool :Boolean) {
        sessionManager.saveDarkTheme(bool)
        _isDarkThemeState.value = bool
    }


    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            val destination = if (sessionManager.isLoggedIn()) {
                Screen.Wishlists.route
            } else {
                Screen.Login.route
            }
            _startDestination.value = destination
        }
    }
    fun editName(newName: String){
        sessionManager.saveUserName(newName)
    }
    fun getUserName(): String? {
        return sessionManager.getUserName()
    }

}