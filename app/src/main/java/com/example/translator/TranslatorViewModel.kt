package com.example.translator

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TranslatorViewModel : ViewModel() {
    private val TAG = "TranslatorViewModel"

    private lateinit var translationManager: TranslationManager
    private lateinit var historyManager: TranslationHistoryManager
    private lateinit var ttsManager: TextToSpeechManager
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

    private val _sourceLanguage = MutableStateFlow(TranslateLanguage.ENGLISH)
    val sourceLanguage: StateFlow<String> = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow(TranslateLanguage.VIETNAMESE)
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    private val _translationHistory = MutableStateFlow<List<TranslationHistoryItem>>(emptyList())
    val translationHistory: StateFlow<List<TranslationHistoryItem>> = _translationHistory.asStateFlow()

    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()

    // Khởi tạo các manager
    fun initManagers(context: Context) {
        if (!::translationManager.isInitialized) {
            translationManager = TranslationManager(context)
        }

        if (!::historyManager.isInitialized) {
            historyManager = TranslationHistoryManager(context)
            viewModelScope.launch {
                historyManager.getAllHistory().collect { history ->
                    _translationHistory.value = history
                }
            }
        }

        if (!::ttsManager.isInitialized) {
            ttsManager = TextToSpeechManager(context)
        }

        if (!::shareManager.isInitialized) {
            shareManager = ShareManager(context)
        }
    }

    // Cập nhật văn bản nguồn
    fun updateSourceText(text: String) {
        _sourceText.value = text
    }

    // Đặt ngôn ngữ nguồn
    suspend fun setSourceLanguage(language: String) {
        if (_sourceLanguage.value != language) {
            _sourceLanguage.value = language
            // Xóa kết quả dịch cũ khi thay đổi ngôn ngữ
            _translatedText.value = ""
            // Tải mô hình mới nếu cần
            downloadModelIfNeeded()
        }
    }

    // Đặt ngôn ngữ đích
    suspend fun setTargetLanguage(language: String) {
        if (_targetLanguage.value != language) {
            _targetLanguage.value = language
            // Xóa kết quả dịch cũ khi thay đổi ngôn ngữ
            _translatedText.value = ""
            // Tải mô hình mới nếu cần
            downloadModelIfNeeded()
        }
    }

    // Đổi ngôn ngữ nguồn và đích
    suspend fun swapLanguages() {
        val tempSourceLang = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = tempSourceLang

        // Xóa kết quả dịch cũ khi đổi ngôn ngữ
        _translatedText.value = ""

        // Tải mô hình mới nếu cần
        downloadModelIfNeeded()
    }

    // Tải mô hình ngôn ngữ nếu cần
    private suspend fun downloadModelIfNeeded(): Boolean {
        return try {
            translationManager.downloadModelIfNeeded(
                _sourceLanguage.value,
                _targetLanguage.value
            ) { state ->
                when (state) {
                    is TranslationManager.ModelState.Loading -> {
                        _isModelDownloading.value = true
                    }
                    is TranslationManager.ModelState.Ready -> {
                        _isModelDownloading.value = false
                    }
                    is TranslationManager.ModelState.Error -> {
                        _isModelDownloading.value = false
                        Log.e(TAG, "Model download error: ${state.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model", e)
            _isModelDownloading.value = false
            false
        }
    }

    // Thực hiện dịch văn bản
    suspend fun translate() {
        if (_sourceText.value.isBlank()) {
            _translatedText.value = ""
            return
        }

        try {
            // Đảm bảo mô hình đã được tải
            val modelReady = downloadModelIfNeeded()
            if (!modelReady) {
                Log.e(TAG, "Model not ready, cannot translate")
                return
            }

            // Thực hiện dịch
            val result = translationManager.translate(
                _sourceText.value,
                _sourceLanguage.value,
                _targetLanguage.value
            ) { state ->
                when (state) {
                    is TranslationManager.TranslationState.Idle -> {
                        _isTranslating.value = false
                    }
                    is TranslationManager.TranslationState.Translating -> {
                        _isTranslating.value = true
                    }
                    is TranslationManager.TranslationState.Success -> {
                        _isTranslating.value = false
                        _translatedText.value = state.translatedText
                    }
                    is TranslationManager.TranslationState.Error -> {
                        _isTranslating.value = false
                        Log.e(TAG, "Translation error: ${state.message}")
                    }
                }
            }

            // Lưu vào lịch sử nếu dịch thành công
            if (result.isNotBlank()) {
                historyManager.addToHistory(
                    _sourceText.value,
                    result,
                    _sourceLanguage.value,
                    _targetLanguage.value
                )
            }
        } catch (e: Exception) {
            _isTranslating.value = false
            Log.e(TAG, "Error during translation", e)
        }
    }

    // Phát âm văn bản đã dịch
    fun speakTranslatedText() {
        if (_translatedText.value.isBlank()) return

        viewModelScope.launch {
            _isSpeaking.value = true

            val state = ttsManager.speak(_translatedText.value, _targetLanguage.value)

            when (state) {
                is TextToSpeechManager.TtsState.Speaking -> {
                    // Đang phát âm, không cần làm gì
                }
                is TextToSpeechManager.TtsState.Completed -> {
                    _isSpeaking.value = false
                }
                is TextToSpeechManager.TtsState.Error -> {
                    _isSpeaking.value = false
                    Log.e(TAG, "TTS error: ${state.message}")
                }
                else -> {
                    _isSpeaking.value = false
                }
            }
        }
    }

    // Dừng phát âm
    fun stopSpeaking() {
        ttsManager.stop()
        _isSpeaking.value = false
    }

    // Chia sẻ bản dịch
    fun shareTranslation() {
        if (_sourceText.value.isBlank() || _translatedText.value.isBlank()) return

        shareManager.shareTranslation(
            _sourceText.value,
            _translatedText.value,
            getLanguageDisplayName(_sourceLanguage.value),
            getLanguageDisplayName(_targetLanguage.value)
        )
    }

    // Hiển thị/ẩn lịch sử dịch
    fun toggleHistoryView() {
        _showHistory.value = !_showHistory.value
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
        viewModelScope.launch {
            historyManager.deleteFromHistory(item)
        }
    }

    // Xóa tất cả lịch sử
    fun clearHistory() {
        viewModelScope.launch {
            historyManager.clearHistory()
        }
    }

    // Lấy tên hiển thị của ngôn ngữ
    private fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            TranslateLanguage.ENGLISH -> "Tiếng Anh"
            TranslateLanguage.VIETNAMESE -> "Tiếng Việt"
            TranslateLanguage.CHINESE -> "Tiếng Trung"
            TranslateLanguage.JAPANESE -> "Tiếng Nhật"
            TranslateLanguage.KOREAN -> "Tiếng Hàn"
            TranslateLanguage.FRENCH -> "Tiếng Pháp"
            TranslateLanguage.GERMAN -> "Tiếng Đức"
            TranslateLanguage.RUSSIAN -> "Tiếng Nga"
            TranslateLanguage.SPANISH -> "Tiếng Tây Ban Nha"
            TranslateLanguage.ITALIAN -> "Tiếng Ý"
            else -> languageCode.replaceFirstChar { it.uppercase() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::translationManager.isInitialized) {
            translationManager.close()
        }
        if (::ttsManager.isInitialized) {
            ttsManager.shutdown()
        }
    }
}
