package com.example.wishlistapp.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.wishlistapp.data.demo.DemoAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(private val prefs: SharedPreferences, private val allowDemo: Boolean = false) {
    private val currentUserId = MutableStateFlow(getUserId())
    val userId = currentUserId.asStateFlow()
    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_DEMO = "demo_account"
    }

    fun saveToken(token: String) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            remove(KEY_DEMO)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
        }
        currentUserId.value = null
    }
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun isDemoSession() = allowDemo && prefs.getBoolean(KEY_DEMO, false)
    fun isLoggedIn() = !getToken().isNullOrBlank() || isDemoSession()
    fun enterDemo(account: DemoAccount) {
        check(allowDemo) { "Demo accounts are disabled for this build" }
        prefs.edit {
            remove(KEY_TOKEN)
            putBoolean(KEY_DEMO, true)
            putInt(KEY_USER_ID, account.userId)
            putString(KEY_USER_NAME, account.displayName)
        }
        currentUserId.value = account.userId
    }
    fun saveUser(id: Int, userName: String) {
        prefs.edit { putInt(KEY_USER_ID, id); putString(KEY_USER_NAME, userName) }
        currentUserId.value = id
    }
    fun getUserId(): Int? = prefs.takeUnless { it.getBoolean(KEY_DEMO, false) && !allowDemo }
        ?.takeIf { it.contains(KEY_USER_ID) }?.getInt(KEY_USER_ID, 0)
    fun getUserName(): String? = prefs.takeUnless { it.getBoolean(KEY_DEMO, false) && !allowDemo }
        ?.getString(KEY_USER_NAME, null)
    fun saveDarkTheme(enabled: Boolean) = prefs.edit { putBoolean(KEY_DARK_THEME, enabled) }
    fun isDarkTheme() = prefs.getBoolean(KEY_DARK_THEME, false)
    fun clear() {
        prefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_DEMO)
        }
        currentUserId.value = null
    }
}
