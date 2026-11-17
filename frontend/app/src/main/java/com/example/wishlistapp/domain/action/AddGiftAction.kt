package com.example.wishlistapp.domain.action

import com.example.wishlistapp.data.repository.GiftRepository
import com.example.wishlistapp.domain.model.Gift
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult

class AddGiftAction(private val repository: GiftRepository) {
    suspend operator fun invoke(wishlistId: Int, name: String, price: String, description: String, link: String): AppResult<Gift> {
        if (name.isBlank()) return AppResult.Failure(AppError.Validation("Укажите название подарка"))
        val numericPrice = price.takeIf { it.isNotBlank() }?.replace(',', '.')?.toDoubleOrNull()
        if (price.isNotBlank() && (numericPrice == null || numericPrice < 0)) {
            return AppResult.Failure(AppError.Validation("Укажите корректную цену"))
        }
        return repository.createGift(wishlistId, name.trim(), numericPrice, description.ifBlank { null }, link.ifBlank { null })
    }
}
