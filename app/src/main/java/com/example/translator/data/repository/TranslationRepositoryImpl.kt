package com.example.translator.data.repository

import com.example.translator.data.datasource.local.TranslationDao
import com.example.translator.data.datasource.remote.TranslationApi
import com.example.translator.domain.model.Translation
import com.example.translator.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TranslationRepositoryImpl @Inject constructor(
    private val api: TranslationApi,
    private val dao: TranslationDao
) : TranslationRepository {

    override suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<Translation> {
        return try {
            val response = api.translate(text, sourceLanguage, targetLanguage)
            val translation = Translation(
                sourceText = text,
                translatedText = response.translatedText,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage
            )
            saveTranslation(translation)
            Result.success(translation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTranslationHistory(): Flow<List<Translation>> {
        return dao.getAllTranslations()
    }

    override suspend fun saveTranslation(translation: Translation) {
        dao.insertTranslation(translation)
    }

    override suspend fun deleteTranslation(translation: Translation) {
        dao.deleteTranslation(translation)
    }
} 