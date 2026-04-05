package com.example.wishlistapp.data.remote

import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import java.io.IOException
import retrofit2.HttpException

suspend fun <T> remoteCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (error: HttpException) {
    AppResult.Failure(
        when (error.code()) {
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            404 -> AppError.NotFound
            409 -> AppError.Conflict
            422 -> AppError.Validation("Проверьте введённые данные")
            else -> AppError.Unknown("HTTP ${error.code()}")
        }
    )
} catch (_: IOException) {
    AppResult.Failure(AppError.Network)
} catch (error: Exception) {
    AppResult.Failure(AppError.Unknown(error.message))
}
