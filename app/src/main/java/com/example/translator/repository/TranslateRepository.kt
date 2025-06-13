package com.example.translator.repository

import com.example.translator.model.Language
import com.example.translator.service.TranslateApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TranslateRepository(private val apiService: TranslateApiService) {

    fun getSupportedLanguages(): List<Language> {
        return Language.values().toList()
    }

    suspend fun translateText(text: String, sourceLang: String, targetLang: String): String {
        return withContext(Dispatchers.IO) {
            val langPair = "$sourceLang|$targetLang"
            val response = apiService.translate(text, langPair)
            response.responseData.translatedText
        }
    }
}