package com.example.wishlistapp.data

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "session_prefs",
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        prefs.edit {
            putString("token", token)
        }
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun saveDarkTheme(enabled: Boolean) {
        prefs.edit {
            putBoolean("dark_theme", enabled)
        }
    }

    fun isDarkTheme(): Boolean {
        return prefs.getBoolean("dark_theme", false)
    }

    fun clear() {
        prefs.edit {
            clear()
        }
    }
}
