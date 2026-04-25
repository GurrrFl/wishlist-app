package com.example.wishlistapp.viewmodel

import com.example.wishlistapp.domain.result.AppError

fun AppError.toUserMessage(): String = when (this) {
    AppError.Network -> "Нет соединения с сервером"
    AppError.Unauthorized -> "Войдите в аккаунт снова"
    AppError.Forbidden -> "Недостаточно прав для этого действия"
    AppError.NotFound -> "Запись не найдена"
    AppError.Conflict -> "Действие конфликтует с текущим состоянием"
    is AppError.Validation -> message
    is AppError.Unknown -> "Не удалось выполнить операцию"
}
