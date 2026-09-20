package com.example.wishlistapp.data.remote

import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CancellationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RemoteCallTest {
    @Test fun mapsHttpFailures() = runBlocking {
        val cases = mapOf(
            401 to AppError.Unauthorized, 403 to AppError.Forbidden,
            404 to AppError.NotFound, 409 to AppError.Conflict
        )
        cases.forEach { (code, expected) ->
            val result = remoteCall<Unit> { throw HttpException(Response.error<Unit>(code, "".toResponseBody())) }
            assertEquals(expected, (result as AppResult.Failure).error)
        }
    }

    @Test fun mapsNetworkFailures() = runBlocking {
        val result = remoteCall<Unit> { throw IOException("offline") }
        assertEquals(AppError.Network, (result as AppResult.Failure).error)
    }

    @Test fun mapsValidationAndPreservesCancellation() = runBlocking {
        val validation = HttpException(Response.error<Unit>(422, "".toResponseBody()))
        assertTrue(validation.toAppError() is AppError.Validation)
        assertTrue((remoteCall<Unit> { throw validation } as AppResult.Failure).error is AppError.Validation)

        val cancelled = runCatching { remoteCall<Unit> { throw CancellationException("stopped") } }
        assertTrue(cancelled.exceptionOrNull() is CancellationException)
    }
}
