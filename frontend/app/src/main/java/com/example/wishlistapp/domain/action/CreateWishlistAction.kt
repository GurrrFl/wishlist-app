package com.example.wishlistapp.domain.action

import com.example.wishlistapp.data.repository.WishlistRepository
import com.example.wishlistapp.domain.model.Wishlist
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateWishlistAction(private val repository: WishlistRepository) {
    suspend operator fun invoke(title: String, isPrivate: Boolean, eventDate: String): AppResult<Wishlist> {
        val name = title.trim()
        if (name.isEmpty()) return AppResult.Failure(AppError.Validation("Укажите название списка"))
        val date = runCatching {
            LocalDate.parse(eventDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }.getOrNull() ?: return AppResult.Failure(AppError.Validation("Выберите дату события"))
        return repository.createWishlist(name, date, isPrivate)
    }
}
