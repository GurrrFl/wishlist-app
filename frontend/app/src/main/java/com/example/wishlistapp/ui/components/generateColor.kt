package com.example.wishlistapp.ui.components

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

fun generateRandomColor(): Color {
    val red = Random.nextInt(100, 220)
    val green = Random.nextInt(100, 220)
    val blue = Random.nextInt(100, 220)
    return Color(red, green, blue)
}