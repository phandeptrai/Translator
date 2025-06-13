package com.example.translator.service

import retrofit2.http.GET
import retrofit2.http.Query

interface TranslateApiService {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langpair: String,
        @Query("key") key: String = "c0b4f01b266fd253939d" // Thay YOUR_API_KEY_HERE bằng key bạn nhận được
    ): MyMemoryResponse
}

data class MyMemoryResponse(val responseData: ResponseData)

data class ResponseData(val translatedText: String)