package com.example.translator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.translator.ui.screens.commonLanguages
import com.example.translator.ui.screens.getLanguageName

@Composable
fun LanguageSelector(
    sourceLanguage: String,
    targetLanguage: String,
    supportedLanguages: List<String> = commonLanguages.map { it.code },
    downloadedLanguages: Set<String> = emptySet(),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(getLanguageName(sourceLanguage))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (sourceLanguage !in downloadedLanguages) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Chưa tải xuống",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                }
            }
            DropdownMenu(
                expanded = showSourceLanguageMenu,
                onDismissRequest = { showSourceLanguageMenu = false }
            ) {
                supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(getLanguageName(language))
                                if (language !in downloadedLanguages) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = "Chưa tải xuống",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = { 
                            onSourceLanguageSelected(language)
                            showSourceLanguageMenu = false
                        }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(getLanguageName(targetLanguage))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (targetLanguage !in downloadedLanguages) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Chưa tải xuống",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                }
            }
            DropdownMenu(
                expanded = showTargetLanguageMenu,
                onDismissRequest = { showTargetLanguageMenu = false }
            ) {
                supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(getLanguageName(language))
                                if (language !in downloadedLanguages) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = "Chưa tải xuống",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = { 
                            onTargetLanguageSelected(language)
                            showTargetLanguageMenu = false
                        }
                    )
                }
            }
        }
    }
} 