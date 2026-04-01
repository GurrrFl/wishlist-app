package com.example.wishlistapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "search_history")

class SearchHistoryStorage(private val context: Context) {

    private val HISTORY_KEY = stringSetPreferencesKey("history")

    val historyFlow: Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            prefs[HISTORY_KEY]?.toList() ?: emptyList()
        }

    suspend fun addLink(link: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[HISTORY_KEY]?.toMutableSet() ?: mutableSetOf()
            current.remove(link)
            current.add(link)
            prefs[HISTORY_KEY] = current
        }
    }

    suspend fun removeLink(link: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[HISTORY_KEY]?.toMutableSet() ?: return@edit
            current.remove(link)
            prefs[HISTORY_KEY] = current
        }
    }
}
