package com.example.translator.utils.managers

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class TextToSpeechManager(private val context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun speak(text: String, language: String) {
        textToSpeech?.let { tts ->
            tts.language = Locale(language)
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "TTS_UTTERANCE_ID"
            )
        }
    }

    fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        textToSpeech?.shutdown()
        textToSpeech = null
    }
} 