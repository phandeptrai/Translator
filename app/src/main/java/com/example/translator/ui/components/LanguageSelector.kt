package com.example.translator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.mlkit.nl.translate.TranslateLanguage

@Composable
fun LanguageSelector(
    sourceLanguage: String,
    targetLanguage: String,
    supportedLanguages: List<String>,
    onSourceLanguageSelected: (String) -> Unit,
    onTargetLanguageSelected: (String) -> Unit,
    onSwapLanguages: () -> Unit
) {
    var showSourceLanguageMenu by remember { mutableStateOf(false) }
    var showTargetLanguageMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Column {
                Text("Ngôn ngữ nguồn", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(
                    onClick = { showSourceLanguageMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(getLanguageDisplayName(sourceLanguage))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = showSourceLanguageMenu,
                onDismissRequest = { showSourceLanguageMenu = false }
            ) {
                supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(getLanguageDisplayName(language)) },
                        onClick = { onSourceLanguageSelected(language) }
                    )
                }
            }
        }

        IconButton(
            onClick = onSwapLanguages,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "Đổi ngôn ngữ")
        }

        Box(modifier = Modifier.weight(1f)) {
            Column {
                Text("Ngôn ngữ đích", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(
                    onClick = { showTargetLanguageMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(getLanguageDisplayName(targetLanguage))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = showTargetLanguageMenu,
                onDismissRequest = { showTargetLanguageMenu = false }
            ) {
                supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(getLanguageDisplayName(language)) },
                        onClick = { onTargetLanguageSelected(language) }
                    )
                }
            }
        }
    }
}

private fun getLanguageDisplayName(languageCode: String): String {
    return when (languageCode) {
        "vi" -> "Tiếng Việt"
        "en" -> "Tiếng Anh"
        "ja" -> "Tiếng Nhật"
        "ko" -> "Tiếng Hàn"
        "zh" -> "Tiếng Trung"
        "fr" -> "Tiếng Pháp"
        "de" -> "Tiếng Đức"
        "es" -> "Tiếng Tây Ban Nha"
        "it" -> "Tiếng Ý"
        "ru" -> "Tiếng Nga"
        else -> languageCode
    }
} 