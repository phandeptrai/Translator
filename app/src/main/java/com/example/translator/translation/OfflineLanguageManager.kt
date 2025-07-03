package com.example.translator.translation

import android.content.Context
import android.content.SharedPreferences
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import kotlinx.coroutines.tasks.await

class OfflineLanguageManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("offline_languages", Context.MODE_PRIVATE)

    fun getDownloadedLanguages(): Set<String> {
        return sharedPreferences.getStringSet("downloaded", emptySet()) ?: emptySet()
    }

    suspend fun downloadLanguage(languageCode: String) {
        val model = TranslateRemoteModel.Builder(languageCode).build()
        val conditions = DownloadConditions.Builder().requireWifi().build()
        RemoteModelManager.getInstance().download(model, conditions).await()
        val set = getDownloadedLanguages().toMutableSet()
        set.add(languageCode)
        sharedPreferences.edit().putStringSet("downloaded", set).apply()
    }

    suspend fun deleteLanguage(languageCode: String) {
        val model = TranslateRemoteModel.Builder(languageCode).build()
        RemoteModelManager.getInstance().deleteDownloadedModel(model).await()
        val set = getDownloadedLanguages().toMutableSet()
        set.remove(languageCode)
        sharedPreferences.edit().putStringSet("downloaded", set).apply()
    }

    suspend fun isLanguageDownloaded(languageCode: String): Boolean {
        val model = TranslateRemoteModel.Builder(languageCode).build()
        return RemoteModelManager.getInstance().isModelDownloaded(model).await()
    }
}
