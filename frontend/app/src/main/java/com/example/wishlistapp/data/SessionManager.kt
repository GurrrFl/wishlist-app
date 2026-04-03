package com.example.wishlistapp.data

import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(private val prefs: SharedPreferences) {
    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    fun saveToken(token: String) = prefs.edit { putString(KEY_TOKEN, token) }
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun isLoggedIn() = !getToken().isNullOrBlank()
    fun saveUser(id: Int, userName: String) = prefs.edit { putInt(KEY_USER_ID, id); putString(KEY_USER_NAME, userName) }
    fun getUserId(): Int? = prefs.takeIf { it.contains(KEY_USER_ID) }?.getInt(KEY_USER_ID, 0)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun saveDarkTheme(enabled: Boolean) = prefs.edit { putBoolean(KEY_DARK_THEME, enabled) }
    fun isDarkTheme() = prefs.getBoolean(KEY_DARK_THEME, false)
    fun clear() = prefs.edit {
        remove(KEY_TOKEN)
        remove(KEY_USER_ID)
        remove(KEY_USER_NAME)
    }
}
