package com.example.translator.domain.usecase

import com.example.translator.domain.model.Translation
import com.example.translator.domain.repository.TranslationRepository
import javax.inject.Inject

class TranslateTextUseCase @Inject constructor(
    private val repository: TranslationRepository
) {
    suspend operator fun invoke(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<Translation> {
        return repository.translateText(text, sourceLanguage, targetLanguage)
    }
} 