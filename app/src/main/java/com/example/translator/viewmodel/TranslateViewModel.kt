package com.example.translator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.translator.model.Language
import com.example.translator.repository.TranslateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TranslateViewModel(private val repository: TranslateRepository) : ViewModel() {

    // StateFlow để quản lý danh sách ngôn ngữ
    private val _languages = MutableStateFlow<List<Language>>(emptyList())
    val languages: StateFlow<List<Language>> = _languages.asStateFlow()

    // StateFlow để quản lý kết quả dịch
    private val _translatedText = MutableStateFlow<String?>(null)
    val translatedText: StateFlow<String?> = _translatedText.asStateFlow()

    init {
        // Load danh sách ngôn ngữ khi ViewModel được tạo
        viewModelScope.launch {
            try {
                val supportedLanguages = repository.getSupportedLanguages()
                _languages.value = supportedLanguages
            } catch (e: Exception) {
                // Xử lý lỗi khi lấy danh sách ngôn ngữ (nếu cần)
                _languages.value = emptyList() // Hoặc giá trị mặc định
            }
        }
    }

    fun translate(text: String, sourceLang: String, targetLang: String) {
        viewModelScope.launch {
            try {
                val result = repository.translateText(text, sourceLang, targetLang)
                _translatedText.value = result
            } catch (e: Exception) {
                _translatedText.value = "Lỗi khi dịch: ${e.message}"
            }
        }
    }
}