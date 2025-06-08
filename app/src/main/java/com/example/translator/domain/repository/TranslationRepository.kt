package com.example.translator.domain.repository

import com.example.translator.domain.model.Translation
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {
    suspend fun translateText(text: String, sourceLanguage: String, targetLanguage: String): Result<Translation>
    fun getTranslationHistory(): Flow<List<Translation>>
    suspend fun saveTranslation(translation: Translation)
    suspend fun deleteTranslation(translation: Translation)
} 