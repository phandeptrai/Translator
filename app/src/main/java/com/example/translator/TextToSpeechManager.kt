package com.example.translator

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

class TextToSpeechManager(context: Context) {
    private val TAG = "TextToSpeechManager"

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    // Trạng thái TTS
    sealed class TtsState {
        object Initializing : TtsState()
        object Ready : TtsState()
        object Speaking : TtsState()
        object Completed : TtsState()
        data class Error(val message: String) : TtsState()
    }

    init {
        initTextToSpeech(context)
    }

    // Khởi tạo TextToSpeech
    private fun initTextToSpeech(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                Log.d(TAG, "TextToSpeech initialized successfully")
            } else {
                isInitialized = false
                Log.e(TAG, "Failed to initialize TextToSpeech: $status")
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "TTS started: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "TTS completed: $utteranceId")
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "TTS error: $utteranceId")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                super.onError(utteranceId, errorCode)
                Log.e(TAG, "TTS error: $utteranceId, code: $errorCode")
            }
        })
    }

    // Đặt ngôn ngữ cho TTS
    fun setLanguage(languageCode: String): Boolean {
        if (!isInitialized) return false

        val locale = when (languageCode) {
            "en" -> Locale.ENGLISH
            "vi" -> Locale("vi", "VN")
            "zh" -> Locale.CHINESE
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "ru" -> Locale("ru", "RU")
            "es" -> Locale("es", "ES")
            "it" -> Locale.ITALIAN
            else -> Locale.getDefault()
        }

        val result = textToSpeech?.setLanguage(locale)
        return result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE
    }

    // Chuyển đổi mã ngôn ngữ ML Kit sang mã ngôn ngữ TTS
    fun convertLanguageCode(mlKitCode: String): String {
        return when (mlKitCode) {
            TranslateLanguage.ENGLISH -> "en"
            TranslateLanguage.VIETNAMESE -> "vi"
            TranslateLanguage.CHINESE -> "zh"
            TranslateLanguage.JAPANESE -> "ja"
            TranslateLanguage.KOREAN -> "ko"
            TranslateLanguage.FRENCH -> "fr"
            TranslateLanguage.GERMAN -> "de"
            TranslateLanguage.RUSSIAN -> "ru"
            TranslateLanguage.SPANISH -> "es"
            TranslateLanguage.ITALIAN -> "it"
            else -> "en" // Mặc định là tiếng Anh
        }
    }

    // Phát âm văn bản
    suspend fun speak(text: String, languageCode: String): TtsState = suspendCancellableCoroutine { continuation ->
        if (!isInitialized) {
            continuation.resume(TtsState.Error("TextToSpeech not initialized"))
            return@suspendCancellableCoroutine
        }

        if (text.isBlank()) {
            continuation.resume(TtsState.Error("Text is empty"))
            return@suspendCancellableCoroutine
        }

        val ttsLanguageCode = convertLanguageCode(languageCode)
        val languageAvailable = setLanguage(ttsLanguageCode)

        if (!languageAvailable) {
            continuation.resume(TtsState.Error("Language not available: $languageCode"))
            return@suspendCancellableCoroutine
        }

        val utteranceId = UUID.randomUUID().toString()

        // Sử dụng Bundle thay vì HashMap
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (!continuation.isCompleted) {
                    continuation.resume(TtsState.Speaking)
                }
            }

            override fun onDone(utteranceId: String?) {
                if (!continuation.isCompleted) {
                    continuation.resume(TtsState.Completed)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (!continuation.isCompleted) {
                    continuation.resume(TtsState.Error("TTS error"))
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                super.onError(utteranceId, errorCode)
                if (!continuation.isCompleted) {
                    continuation.resume(TtsState.Error("TTS error: $errorCode"))
                }
            }
        })

        val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

        if (result == TextToSpeech.ERROR) {
            continuation.resume(TtsState.Error("Failed to queue text for speaking"))
        }

        continuation.invokeOnCancellation {
            textToSpeech?.stop()
        }
    }

    // Dừng phát âm
    fun stop() {
        textToSpeech?.stop()
    }

    // Giải phóng tài nguyên
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
