package com.example.translator.translation

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class TranslationManager(private val context: Context) {
    private var translator: Translator? = null
    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating

    private val _isModelDownloading = MutableStateFlow(false)
    val isModelDownloading: StateFlow<Boolean> = _isModelDownloading

    suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String {
        if (text.isBlank()) return ""

        _isTranslating.value = true
        try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()

            translator = Translation.getClient(options)

            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            _isModelDownloading.value = true
            translator?.downloadModelIfNeeded(conditions)?.await()
            _isModelDownloading.value = false

            return translator?.translate(text)?.await() ?: ""
        } finally {
            _isTranslating.value = false
        }
    }

    fun shutdown() {
        translator?.close()
        translator = null
    }
} 