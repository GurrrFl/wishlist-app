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

    /**
     * Получает сохраненный токен.
     * Эту функцию нужно вызывать, например, в Interceptor'е для Retrofit,
     * чтобы добавлять токен в заголовки сетевых запросов.
     */
    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    /**
     * Очищает все данные сессии (например, при выходе из аккаунта).
     * Используется KTX-расширение [edit] для более чистого синтаксиса.
     */
    fun clear() {
        prefs.edit {
            clear()
        }
    }
}
