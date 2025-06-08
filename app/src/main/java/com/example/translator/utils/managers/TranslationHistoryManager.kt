package com.example.translator.utils.managers

import android.content.Context
import android.content.SharedPreferences
import com.example.translator.ui.screens.TranslationHistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TranslationHistoryManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "translation_history",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val _history = MutableStateFlow<List<TranslationHistoryItem>>(emptyList())
    val history: StateFlow<List<TranslationHistoryItem>> = _history

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val json = sharedPreferences.getString("history", "[]")
        val type = object : TypeToken<List<TranslationHistoryItem>>() {}.type
        val loadedHistory = gson.fromJson<List<TranslationHistoryItem>>(json, type)
        _history.value = loadedHistory
    }

    private fun saveHistory() {
        val json = gson.toJson(_history.value)
        sharedPreferences.edit().putString("history", json).apply()
    }

    fun addToHistory(
        sourceText: String,
        translatedText: String,
        sourceLanguage: String,
        targetLanguage: String
    ) {
        val newItem = TranslationHistoryItem(
            sourceText = sourceText,
            translatedText = translatedText,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            timestamp = System.currentTimeMillis()
        )

        val currentHistory = _history.value.toMutableList()
        currentHistory.add(0, newItem)
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }
        _history.value = currentHistory
        saveHistory()
    }

    fun deleteHistoryItem(item: TranslationHistoryItem) {
        val currentHistory = _history.value.toMutableList()
        currentHistory.remove(item)
        _history.value = currentHistory
        saveHistory()
    }

    fun clearHistory() {
        _history.value = emptyList()
        saveHistory()
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 50
    }
} 