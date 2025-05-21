package com.example.translator

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class ShareManager(private val context: Context) {

    // Chia sẻ văn bản
    fun shareText(text: String, title: String = "Chia sẻ bản dịch") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, title)
        ContextCompat.startActivity(context, shareIntent, null)
    }

    // Chia sẻ cả văn bản nguồn và bản dịch
    fun shareTranslation(sourceText: String, translatedText: String, sourceLanguage: String, targetLanguage: String) {
        val formattedText = buildString {
            append("Văn bản gốc ($sourceLanguage):\n")
            append(sourceText)
            append("\n\n")
            append("Bản dịch ($targetLanguage):\n")
            append(translatedText)
        }

        shareText(formattedText)
    }
}
