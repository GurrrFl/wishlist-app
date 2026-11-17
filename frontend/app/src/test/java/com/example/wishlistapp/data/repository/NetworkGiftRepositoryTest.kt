package com.example.wishlistapp.data.repository

import com.example.wishlistapp.data.model.dto.CreateGiftDto
import com.example.wishlistapp.data.model.dto.GiftDto
import com.example.wishlistapp.data.model.dto.UpdateGiftDto
import com.example.wishlistapp.data.remote.GiftApi
import com.example.wishlistapp.domain.result.AppError
import com.example.wishlistapp.domain.result.AppResult
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkGiftRepositoryTest {
    @Test fun createsAndDeletesWithoutConcreteWishlistRepository() = runBlocking {
        var createdRequest: CreateGiftDto? = null
        var deletedId: Int? = null
        val api = object : GiftApi {
            override suspend fun getByWishlist(wishlistId: Int): List<GiftDto> = emptyList()
            override suspend fun getGift(id: Int): GiftDto = error("unused")
            override suspend fun create(request: CreateGiftDto): GiftDto {
                createdRequest = request
                return GiftDto(9, request.wishlistId, request.name, request.price, request.description, request.storeLink, "available", null)
            }
            override suspend fun update(id: Int, request: UpdateGiftDto): GiftDto = error("unused")
            override suspend fun delete(id: Int) { deletedId = id }
        }
        val repository: GiftRepository = NetworkGiftRepository(api)
        val created = repository.createGift(4, "Book", 12.5, null, "https://example.org") as AppResult.Success
        assertEquals(4, createdRequest?.wishlistId)
        assertEquals(9, created.data.id)
        assertTrue(repository.deleteGift(9) is AppResult.Success)
        assertEquals(9, deletedId)
    }

    @Test fun preservesForbiddenResponseFromBackend() = runBlocking {
        val api = object : GiftApi {
            override suspend fun getByWishlist(wishlistId: Int): List<GiftDto> = emptyList()
            override suspend fun getGift(id: Int): GiftDto = error("unused")
            override suspend fun create(request: CreateGiftDto): GiftDto =
                throw HttpException(Response.error<GiftDto>(403, "".toResponseBody()))
            override suspend fun update(id: Int, request: UpdateGiftDto): GiftDto = error("unused")
            override suspend fun delete(id: Int) = error("unused")
        }
        val result = NetworkGiftRepository(api).createGift(4, "Book", null, null, null)
        assertEquals(AppError.Forbidden, (result as AppResult.Failure).error)
    }
}
