package com.example.wishlistapp.data

import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(private val prefs: SharedPreferences) {

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    fun saveToken(token: String) {
        prefs.edit {
            putString(KEY_TOKEN, token)
        }
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun saveDarkTheme(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_DARK_THEME, enabled)
        }
    }

    fun isDarkTheme(): Boolean {
        return prefs.getBoolean(KEY_DARK_THEME, false)
    }

    fun clear() {
        prefs.edit {
            clear()
        }
    }
    fun saveUserName(userName: String) {
        prefs.edit {
            putString(KEY_USER_NAME, userName)
        }
    }
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, "User")
    }
}
