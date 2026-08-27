package com.example.wishlistapp.data.remote

import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

suspend fun <T> remoteCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    AppResult.Failure(error.toAppError())
}

fun Throwable.toAppError(): AppError = when (this) {
    is HttpException -> when (code()) {
        401 -> AppError.Unauthorized
        403 -> AppError.Forbidden
        404 -> AppError.NotFound
        409 -> AppError.Conflict
        422 -> AppError.Validation("Проверьте введённые данные")
        else -> AppError.Unknown("HTTP ${code()}")
    }
    is IOException -> AppError.Network
    else -> AppError.Unknown(message)
}
