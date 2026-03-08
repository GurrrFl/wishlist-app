package com.example.wishlistapp.ui.components

import androidx.compose.runtime.Composable

fun obtainWishlist(title : String,
                   description : String,
                   eventDate :String,

): Result<String> {
    return if (title.isEmpty()) Result.failure(Exception("Название не может быть пустым"))
    else if (eventDate.isEmpty()) Result.failure(Exception("Дата не может быть пустой"))
    else if (description.isEmpty()) Result.failure(Exception("Описание не может быть пустым"))
    else Result.success("Ура вишлист")
}
