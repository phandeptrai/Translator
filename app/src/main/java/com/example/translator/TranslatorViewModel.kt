package com.example.translator

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import com.example.translator.ui.screens.TranslationHistoryItem
import com.example.translator.translation.*
import com.example.translator.texttospeech.*
import com.example.translator.utils.managers.*
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

// Map để lấy tên ngôn ngữ tiếng Việt
private val languageNameMap = mapOf(
    "vi" to "Tiếng Việt",
    "en" to "Tiếng Anh",
    "ja" to "Tiếng Nhật",
    "zh" to "Tiếng Trung",
    "ko" to "Tiếng Hàn",
    "fr" to "Tiếng Pháp",
    "pt" to "Tiếng Bồ Đào Nha"
)

private fun getLanguageName(code: String): String {
    return languageNameMap[code] ?: code.uppercase()
}

class TranslatorViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "TranslatorViewModel"

    private lateinit var translationManager: TranslationManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private lateinit var translationHistoryManager: TranslationHistoryManager
    private lateinit var shareManager: ShareManager
    private lateinit var offlineLanguageManager: OfflineLanguageManager

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

    private val _downloadedLanguages = MutableStateFlow<Set<String>>(emptySet())
    val downloadedLanguages: StateFlow<Set<String>> = _downloadedLanguages.asStateFlow()

    private val _downloadingLanguage = MutableStateFlow<String?>(null)
    val downloadingLanguage: StateFlow<String?> = _downloadingLanguage.asStateFlow()

    private var translateJob: Job? = null

    // Khởi tạo các manager
    fun initManagers(context: Context) {
        translationManager = TranslationManager(context)
        textToSpeechManager = TextToSpeechManager(context)
        translationHistoryManager = TranslationHistoryManager(context)
        shareManager = ShareManager(context)
        offlineLanguageManager = OfflineLanguageManager(context)
        
        // Tải danh sách ngôn ngữ đã tải xuống
        loadDownloadedLanguages()
        
        // Kiểm tra và xử lý văn bản từ Intent (tính năng dịch khi bôi đen)
        handleSharedText()
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
            if (_sourceText.value.isBlank()) {
                return@launch
            }
            
            // Kiểm tra mô hình offline cho cả ngôn ngữ nguồn và đích
            val sourceDownloaded = offlineLanguageManager.isLanguageDownloaded(_sourceLanguage.value)
            val targetDownloaded = offlineLanguageManager.isLanguageDownloaded(_targetLanguage.value)
            
            if (!sourceDownloaded || !targetDownloaded) {
                val missingLanguages = mutableListOf<String>()
                if (!sourceDownloaded) {
                    missingLanguages.add(getLanguageName(_sourceLanguage.value))
                }
                if (!targetDownloaded) {
                    missingLanguages.add(getLanguageName(_targetLanguage.value))
                }
                
                val message = if (missingLanguages.size == 1) {
                    "Bạn cần tải xuống ngôn ngữ ${missingLanguages[0]} để dịch offline. Bấm vào icon tải xuống để tải ngôn ngữ."
                } else {
                    "Bạn cần tải xuống các ngôn ngữ: ${missingLanguages.joinToString(", ")} để dịch offline. Bấm vào icon tải xuống để tải ngôn ngữ."
                }
                
                Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show()
                _translatedText.value = ""
                return@launch
            }
            
            _isTranslating.value = true
            try {
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
            } catch (e: Exception) {
                Log.e(TAG, "Translation error: ${e.message}")
                Toast.makeText(
                    getApplication(),
                    "Lỗi khi dịch: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                _translatedText.value = ""
            } finally {
                _isTranslating.value = false
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

    // Sử dụng một mục từ lịch sử
    fun useHistoryItem(item: TranslationHistoryItem) {
        _sourceText.value = item.sourceText
        _translatedText.value = item.translatedText
        _sourceLanguage.value = item.sourceLanguage
        _targetLanguage.value = item.targetLanguage
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

    // Tải xuống ngôn ngữ offline
    fun downloadLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                _downloadingLanguage.value = languageCode
                _isModelDownloading.value = true
                offlineLanguageManager.downloadLanguage(languageCode)
                loadDownloadedLanguages()
                Toast.makeText(
                    getApplication(),
                    "Đã tải xuống ngôn ngữ $languageCode",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading language: ${e.message}")
                Toast.makeText(
                    getApplication(),
                    "Lỗi khi tải xuống ngôn ngữ: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                _downloadingLanguage.value = null
                _isModelDownloading.value = false
            }
        }
    }

    // Xóa ngôn ngữ offline
    fun deleteLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                offlineLanguageManager.deleteLanguage(languageCode)
                loadDownloadedLanguages()
                Toast.makeText(
                    getApplication(),
                    "Đã xóa ngôn ngữ $languageCode",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting language: ${e.message}")
                Toast.makeText(
                    getApplication(),
                    "Lỗi khi xóa ngôn ngữ: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Tải danh sách ngôn ngữ đã tải xuống
    fun loadDownloadedLanguages() {
        viewModelScope.launch {
            _downloadedLanguages.value = offlineLanguageManager.getDownloadedLanguages()
        }
    }

    // Xử lý văn bản nhận được từ Intent (tính năng dịch khi chia sẻ)
    private fun handleSharedText() {
        val sharedText = SharedTextManager.getText()
        if (!sharedText.isNullOrBlank()) {
            // Tự động cập nhật văn bản nguồn và dịch
            updateSourceText(sharedText)
            
            // Tự động chuyển sang ngôn ngữ phù hợp nếu cần
            autoDetectAndSetLanguage(sharedText)
        }
    }

    // Tự động phát hiện ngôn ngữ và đặt ngôn ngữ nguồn phù hợp
    private fun autoDetectAndSetLanguage(text: String) {
        // Logic đơn giản: nếu có ký tự tiếng Việt thì đặt nguồn là Việt, ngược lại là Anh
        val hasVietnameseChars = text.any { it in 'à'..'ỹ' || it in 'À'..'Ỹ' }
        val hasEnglishChars = text.any { it.isLetter() && it in 'a'..'z' || it in 'A'..'Z' }
        
        when {
            hasVietnameseChars -> {
                _sourceLanguage.value = "vi"
                _targetLanguage.value = "en"
            }
            hasEnglishChars -> {
                _sourceLanguage.value = "en"
                _targetLanguage.value = "vi"
            }
            // Nếu không phát hiện được, giữ nguyên cài đặt hiện tại
        }
    }

    // Hàm public để MainActivity có thể gọi khi nhận Intent mới
    fun checkForSharedText() {
        handleSharedText()
    }

    override fun onCleared() {
        super.onCleared()
        translationManager.shutdown()
        textToSpeechManager.shutdown()
    }
}
