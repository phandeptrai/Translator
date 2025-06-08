package com.example.translator.data.datasource.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApi {
    @GET("translate")
    suspend fun translate(
        @Query("text") text: String,
        @Query("source") sourceLanguage: String,
        @Query("target") targetLanguage: String
    ): TranslationResponse
}

data class TranslationResponse(
    val translatedText: String
) 