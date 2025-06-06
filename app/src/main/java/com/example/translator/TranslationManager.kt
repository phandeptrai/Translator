package com.example.translator

import android.content.Context
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TranslationManager(private val context: Context) {
    private val TAG = "TranslationManager"

    // Lưu trữ các translator đã tạo để tái sử dụng
    private val translators = mutableMapOf<Pair<String, String>, Translator>()

    // Trạng thái tải mô hình
    sealed class ModelState {
        object Loading : ModelState()
        object Ready : ModelState()
        data class Error(val message: String) : ModelState()
    }

    // Trạng thái dịch
    sealed class TranslationState {
        object Idle : TranslationState()
        object Translating : TranslationState()
        data class Success(val translatedText: String) : TranslationState()
        data class Error(val message: String) : TranslationState()
    }

    // Tạo translator với ngôn ngữ nguồn và đích
    private fun getTranslator(sourceLanguage: String, targetLanguage: String): Translator {
        val languagePair = Pair(sourceLanguage, targetLanguage)

        return translators.getOrPut(languagePair) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()

            Translation.getClient(options)
        }
    }

    // Tải mô hình ngôn ngữ nếu cần
    suspend fun downloadModelIfNeeded(
        sourceLanguage: String,
        targetLanguage: String,
        onStateChange: (ModelState) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            onStateChange(ModelState.Loading)

            val translator = getTranslator(sourceLanguage, targetLanguage)
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            suspendCancellableCoroutine { continuation ->
                translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        Log.d(TAG, "Model downloaded successfully")
                        onStateChange(ModelState.Ready)
                        continuation.resume(true)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Model download failed", exception)
                        onStateChange(ModelState.Error(exception.message ?: "Unknown error"))
                        continuation.resume(false)
                    }

                continuation.invokeOnCancellation {
                    // Không cần hủy tải mô hình, nhưng có thể thêm xử lý nếu cần
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model", e)
            onStateChange(ModelState.Error(e.message ?: "Unknown error"))
            false
        }
    }

    // Thực hiện dịch văn bản
    suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        onStateChange: (TranslationState) -> Unit
    ): String = withContext(Dispatchers.IO) {
        try {
            if (text.isBlank()) {
                onStateChange(TranslationState.Success(""))
                return@withContext ""
            }

            onStateChange(TranslationState.Translating)

            val translator = getTranslator(sourceLanguage, targetLanguage)

            suspendCancellableCoroutine { continuation ->
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        Log.d(TAG, "Translation successful: $translatedText")
                        onStateChange(TranslationState.Success(translatedText))
                        continuation.resume(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Translation failed", exception)
                        onStateChange(TranslationState.Error(exception.message ?: "Unknown error"))
                        continuation.resumeWithException(exception)
                    }

                continuation.invokeOnCancellation {
                    // Không thể hủy quá trình dịch đang diễn ra, nhưng có thể thêm xử lý nếu cần
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during translation", e)
            onStateChange(TranslationState.Error(e.message ?: "Unknown error"))
            throw e
        }
    }

    // Giải phóng tài nguyên khi không cần nữa
    fun close() {
        translators.values.forEach { translator ->
            translator.close()
        }
        translators.clear()
    }
}
