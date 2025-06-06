package com.example.translator

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class FirebaseTTS(private val context: Context) {
    private val tts: TextToSpeech
    private var isInitialized = false
    private val TAG = "FirebaseTTS"

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                Log.d(TAG, "TextToSpeech khởi tạo thành công")
            } else {
                Log.e(TAG, "Lỗi khởi tạo TextToSpeech: $status")
            }
        }
    }

    suspend fun speak(text: String, languageCode: String = "vi-VN") = withContext(Dispatchers.IO) {
        if (!isInitialized || text.isBlank()) return@withContext

        try {
            // Chuyển đổi mã ngôn ngữ sang Locale
            val locale = when (languageCode) {
                "en-US" -> Locale.US
                "vi-VN" -> Locale("vi", "VN")
                else -> Locale.getDefault()
            }

            withContext(Dispatchers.Main) {
                // Đặt ngôn ngữ
                val result = tts.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Ngôn ngữ không được hỗ trợ: $languageCode")
                    return@withContext
                }

                // Phát âm
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi phát âm: ${e.message}", e)
        }
    }

    fun stop() {
        if (tts.isSpeaking) {
            tts.stop()
        }
    }

    fun release() {
        tts.stop()
        tts.shutdown()
    }
}