package com.example.translator.api

import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApi {
    @GET("language/translate/v2")
    suspend fun translate(
        @Query("q") text: String,
        @Query("source") sourceLang: String,
        @Query("target") targetLang: String,
        @Query("key") apiKey: String
    ): TranslationResponse
}

data class TranslationResponse(
    val data: TranslationData
)

data class TranslationData(
    val translations: List<TranslatedText>
)

data class TranslatedText(
    val translatedText: String
)