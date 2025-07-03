package com.example.translator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.translator.TranslatorViewModel
import kotlinx.coroutines.launch

// Hệ thống ngôn ngữ dễ mở rộng
data class LanguageInfo(
    val code: String,
    val name: String,
    val isCommon: Boolean = false
)

// Danh sách ngôn ngữ thông dụng
val commonLanguages = listOf(
    LanguageInfo("en", "Tiếng Anh", true),
    LanguageInfo("ja", "Tiếng Nhật", true),
    LanguageInfo("zh", "Tiếng Trung", true),
    LanguageInfo("ko", "Tiếng Hàn", true),
    LanguageInfo("fr", "Tiếng Pháp", true),
    LanguageInfo("pt", "Tiếng Bồ Đào Nha", true),
    LanguageInfo("vi", "Tiếng Việt", true)
)

// Map để dễ dàng tìm kiếm tên ngôn ngữ
val languageNameMap = commonLanguages.associate { it.code to it.name }

fun getLanguageName(code: String): String {
    return languageNameMap[code] ?: code.uppercase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineLanguagesScreen(
    viewModel: TranslatorViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val downloadedLanguages by viewModel.downloadedLanguages.collectAsState()
    val isDownloading by viewModel.isModelDownloading.collectAsState()
    val downloadingLanguage by viewModel.downloadingLanguage.collectAsState()
    
    // Chỉ hiển thị ngôn ngữ thông dụng
    val supportedLanguages = remember { commonLanguages }

    LaunchedEffect(Unit) {
        viewModel.loadDownloadedLanguages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ngôn ngữ Offline") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Ngôn ngữ đã tải xuống",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(downloadedLanguages.toList()) { languageCode ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getLanguageName(languageCode),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.deleteLanguage(languageCode)
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Xóa ngôn ngữ",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tải xuống ngôn ngữ mới",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(supportedLanguages.filter { it.code !in downloadedLanguages }) { languageInfo ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = languageInfo.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (downloadingLanguage == languageInfo.code) {
                            Text(
                                text = "Đang tải...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.downloadLanguage(languageInfo.code)
                                    }
                                },
                                enabled = downloadingLanguage == null
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Tải xuống ngôn ngữ"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 