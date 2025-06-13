package com.example.translator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.translator.model.Language
import com.example.translator.ui.components.LanguageDropdown
import com.example.translator.viewmodel.TranslateViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(viewModel: TranslateViewModel) {
    var inputText by remember { mutableStateOf("") }
    var sourceLang by remember { mutableStateOf(Language.ENGLISH.code) } // Mặc định là English
    var targetLang by remember { mutableStateOf(Language.VIETNAMESE.code) } // Mặc định là Vietnamese
    // Sử dụng collectAsState để theo dõi StateFlow
    val translatedText by viewModel.translatedText.collectAsState(initial = null)
    val languages = Language.values().toList() // Lấy toàn bộ ngôn ngữ từ enum

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Ô nhập văn bản
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Nhập văn bản") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Row chứa dropdown ngôn ngữ, nút đổi chỗ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Thêm khoảng cách giữa các phần tử
        ) {
            LanguageDropdown(
                languages = languages,
                selectedLanguageCode = sourceLang,
                onSelectionChange = { sourceLang = it },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    val temp = sourceLang
                    sourceLang = targetLang
                    targetLang = temp
                    viewModel.translate(inputText, sourceLang, targetLang)
                },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = "Swap languages"
                )
            }

            LanguageDropdown(
                languages = languages,
                selectedLanguageCode = targetLang,
                onSelectionChange = { targetLang = it },
                modifier = Modifier.weight(1f)
            )
        }

        // Nút Dịch
        Button(
            onClick = { viewModel.translate(inputText, sourceLang, targetLang) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = inputText.isNotEmpty()
        ) {
            Text("Dịch")
        }

        // Kết quả dịch
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = translatedText ?: "Kết quả dịch sẽ hiển thị ở đây",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}