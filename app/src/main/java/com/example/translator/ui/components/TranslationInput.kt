package com.example.translator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationInput(
    sourceText: String,
    onSourceTextChange: (String) -> Unit,
    isRecording: Boolean,
    onRecordClick: () -> Unit
) {
    OutlinedTextField(
        value = sourceText,
        onValueChange = onSourceTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        label = { Text("Nhập văn bản") },
        trailingIcon = {
            IconButton(onClick = onRecordClick) {
                Icon(
                    if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Dừng ghi âm" else "Bắt đầu ghi âm"
                )
            }
        }
    )
} 