package com.example.translator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.translator.domain.model.Translation
import com.example.translator.domain.usecase.TranslateTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val translateTextUseCase: TranslateTextUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TranslatorUiState>(TranslatorUiState.Initial)
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    fun translateText(text: String, sourceLanguage: String, targetLanguage: String) {
        viewModelScope.launch {
            _uiState.value = TranslatorUiState.Loading
            translateTextUseCase(text, sourceLanguage, targetLanguage)
                .onSuccess { translation ->
                    _uiState.value = TranslatorUiState.Success(translation)
                }
                .onFailure { error ->
                    _uiState.value = TranslatorUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

sealed class TranslatorUiState {
    object Initial : TranslatorUiState()
    object Loading : TranslatorUiState()
    data class Success(val translation: Translation) : TranslatorUiState()
    data class Error(val message: String) : TranslatorUiState()
} 