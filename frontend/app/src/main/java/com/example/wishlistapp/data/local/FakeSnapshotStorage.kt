package com.example.wishlistapp.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson

class FakeSnapshotStorage(private val preferences: SharedPreferences, private val gson: Gson = Gson()) {
    fun load(): FakeSnapshot? {
        val json = preferences.getString(KEY_SNAPSHOT, null) ?: return null
        return runCatching { gson.fromJson(json, FakeSnapshot::class.java) }.getOrNull()
    }

    fun save(snapshot: FakeSnapshot) {
        preferences.edit { putString(KEY_SNAPSHOT, gson.toJson(snapshot)) }
    }

    private companion object { const val KEY_SNAPSHOT = "fake_snapshot_v1" }
}
