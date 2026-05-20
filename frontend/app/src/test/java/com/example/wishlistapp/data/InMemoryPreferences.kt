package com.example.wishlistapp.data

import android.content.SharedPreferences
import java.lang.reflect.Proxy

/** Minimal SharedPreferences implementation for repository unit tests. */
fun inMemoryPreferences(): SharedPreferences {
    val values = mutableMapOf<String, Any>()
    lateinit var editor: SharedPreferences.Editor
    editor = Proxy.newProxyInstance(
        SharedPreferences.Editor::class.java.classLoader,
        arrayOf(SharedPreferences.Editor::class.java)
    ) { _, method, args ->
        when (method.name) {
            "putString", "putInt", "putBoolean" -> { values[args!![0] as String] = args[1]; editor }
            "remove" -> { values.remove(args!![0] as String); editor }
            "clear" -> { values.clear(); editor }
            "apply" -> null
            "commit" -> true
            else -> editor
        }
    } as SharedPreferences.Editor
    return Proxy.newProxyInstance(
        SharedPreferences::class.java.classLoader,
        arrayOf(SharedPreferences::class.java)
    ) { _, method, args ->
        when (method.name) {
            "edit" -> editor
            "contains" -> values.containsKey(args!![0])
            "getString", "getInt", "getBoolean" -> values[args!![0]] ?: args[1]
            "getAll" -> values.toMap()
            else -> null
        }
    } as SharedPreferences
}
