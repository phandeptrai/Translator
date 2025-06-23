package com.example.translator

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import com.example.translator.ui.screens.TranslationHistoryItem
import com.example.translator.translation.*
import com.example.translator.texttospeech.*
import com.example.translator.utils.managers.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class TranslatorViewModel : ViewModel() {
    private val TAG = "TranslatorViewModel"

    private lateinit var translationManager: TranslationManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private lateinit var translationHistoryManager: TranslationHistoryManager
    private lateinit var shareManager: ShareManager

    // Trạng thái UI
    private val _sourceText = MutableStateFlow("")
    val sourceText: StateFlow<String> = _sourceText.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _isModelDownloading = MutableStateFlow(false)
    val isModelDownloading: StateFlow<Boolean> = _isModelDownloading.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _sourceLanguage = MutableStateFlow("vi")
    val sourceLanguage: StateFlow<String> = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow("en")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    private val _translationHistory = MutableStateFlow<List<TranslationHistoryItem>>(emptyList())
    val translationHistory: StateFlow<List<TranslationHistoryItem>> = _translationHistory.asStateFlow()

    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()

    private var translateJob: Job? = null

    // Khởi tạo các manager
    fun initManagers(context: Context) {
        translationManager = TranslationManager(context)
        textToSpeechManager = TextToSpeechManager(context)
        translationHistoryManager = TranslationHistoryManager(context)
        shareManager = ShareManager(context)
    }

    // Cập nhật văn bản nguồn
    fun updateSourceText(text: String) {
        _sourceText.value = text
        debounceTranslate()
    }

    private fun debounceTranslate() {
        translateJob?.cancel()
        translateJob = viewModelScope.launch {
            delay(700)
            translate()
        }
    }

    // Đặt ngôn ngữ nguồn
    fun setSourceLanguage(language: String) {
        _sourceLanguage.value = language
        debounceTranslate()
    }

    // Đặt ngôn ngữ đích
    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
        debounceTranslate()
    }

    // Đổi ngôn ngữ nguồn và đích
    fun swapLanguages() {
        val temp = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = temp
        debounceTranslate()
    }

    // Lấy 5 lịch sử dịch gần nhất
    private fun updateHistory() {
        viewModelScope.launch {
            val history = translationHistoryManager.history.value.take(5)
            _translationHistory.value = history
        }
    }

    // Thực hiện dịch văn bản
    private fun translate() {
        viewModelScope.launch {
            val result = translationManager.translate(
                _sourceText.value,
                _sourceLanguage.value,
                _targetLanguage.value
            )
            _translatedText.value = result

            if (result.isNotBlank()) {
                translationHistoryManager.addToHistory(
                    _sourceText.value,
                    result,
                    _sourceLanguage.value,
                    _targetLanguage.value
                )
                updateHistory()
            }
        }
    }

    // Phát âm văn bản đã dịch
    fun speakText() {
        textToSpeechManager.speak(_translatedText.value, _targetLanguage.value)
    }

    // Chia sẻ bản dịch
    fun shareTranslation() {
        shareManager.shareText(_translatedText.value)
    }

    // Hiển thị/ẩn lịch sử dịch
    fun toggleHistoryView() {
        _showHistory.value = !_showHistory.value
        if (_showHistory.value) {
            updateHistory()
        }
    }

    // Sử dụng một mục từ lịch sử
    fun useHistoryItem(item: TranslationHistoryItem) {
        _sourceText.value = item.sourceText
        _translatedText.value = item.translatedText
        _sourceLanguage.value = item.sourceLanguage
        _targetLanguage.value = item.targetLanguage
        _showHistory.value = false
    }

    // Xóa một mục khỏi lịch sử
    fun deleteHistoryItem(item: TranslationHistoryItem) {
        translationHistoryManager.deleteHistoryItem(item)
        updateHistory()
    }

    // Xóa tất cả lịch sử
    fun clearHistory() {
        translationHistoryManager.clearHistory()
        updateHistory()
    }

    override fun onCleared() {
        super.onCleared()
        translationManager.shutdown()
        textToSpeechManager.shutdown()
    }
}
