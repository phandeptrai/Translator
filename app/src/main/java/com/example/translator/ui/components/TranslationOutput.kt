package com.example.translator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationOutput(
    translatedText: String,
    isTranslating: Boolean,
    isModelDownloading: Boolean,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit,
    onShareClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = translatedText,
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            label = { Text("Bản dịch") },
            readOnly = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = onSpeakClick) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = "Đọc văn bản",
                            tint = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Chia sẻ"
                        )
                    }
                    IconButton(onClick = onExportClick) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Xuất file"
                        )
                    }
                }
            }
        )

        if (isTranslating || isModelDownloading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (isModelDownloading) "Đang tải mô hình..." else "Đang dịch...",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 