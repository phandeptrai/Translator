package com.example.translator

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.translator.ui.screens.VoiceToTextScreen
import com.example.translator.ui.theme.TranslatorTheme
import android.Manifest
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.translator.voicetotext.SpeechRecognizerManager
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAudioPermission()
        setContent {

            TranslatorApp()
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )

            setContent {
                MaterialTheme {
                    TranslatorApp()
                }

            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TranslatorApp(viewModel: TranslatorViewModel = viewModel()) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val coroutineScope = rememberCoroutineScope()

        val sourceText by viewModel.sourceText.collectAsState()
        val translatedText by viewModel.translatedText.collectAsState()
        val isTranslating by viewModel.isTranslating.collectAsState()
        val isModelDownloading by viewModel.isModelDownloading.collectAsState()
        val isSpeaking by viewModel.isSpeaking.collectAsState()

        val sourceLanguage by viewModel.sourceLanguage.collectAsState()
        val targetLanguage by viewModel.targetLanguage.collectAsState()
        val translationHistory by viewModel.translationHistory.collectAsState()
        val showHistory by viewModel.showHistory.collectAsState()

        val supportedLanguages = remember { TranslateLanguage.getAllLanguages().sorted() }

        var showSourceLanguageMenu by remember { mutableStateOf(false) }
        var showTargetLanguageMenu by remember { mutableStateOf(false) }
        var isRecording by remember { mutableStateOf(false) }

        val speechRecognizerManager = remember {
            SpeechRecognizerManager(
                context = context,
                onResult = { result ->
                    viewModel.updateSourceText(result)
                    isRecording = false
                },
                onError = { error ->
                    isRecording = false
                    viewModel.updateSourceText("Error: $error")
                }
            )
        }

        DisposableEffect(Unit) {
            onDispose {
                speechRecognizerManager.destroy()
            }
        }

        LaunchedEffect(Unit) {
            viewModel.initManagers(context)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ứng dụng Dịch") },
                    actions = {
                        IconButton(onClick = { viewModel.toggleHistoryView() }) {
                            Icon(
                                if (showHistory) Icons.Default.Close else Icons.Default.History,
                                contentDescription = if (showHistory) "Đóng lịch sử" else "Xem lịch sử"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (showHistory) {
                HistoryScreen(
                    history = translationHistory,
                    onItemClick = { viewModel.useHistoryItem(it) },
                    onDeleteItem = { viewModel.deleteHistoryItem(it) },
                    onClearHistory = { viewModel.clearHistory() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Phần chọn ngôn ngữ (giữ nguyên như trước)
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
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.setSourceLanguage(language)
                                                showSourceLanguageMenu = false
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.swapLanguages()
                                }
                            },
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
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.setTargetLanguage(language)
                                                showTargetLanguageMenu = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Phần nhập văn bản với nút thu âm
                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = { viewModel.updateSourceText(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        label = { Text("Nhập văn bản") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (isRecording) {
                                        speechRecognizerManager.stopListening()
                                        isRecording = false
                                    } else {
                                        speechRecognizerManager.startListening()
                                        isRecording = true
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Thu âm",
                                    tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                coroutineScope.launch {
                                    viewModel.translate()
                                }
                            }
                        )
                    )

                    // Hiển thị trạng thái khi đang thu âm
                    if (isRecording) {
                        Text(
                            text = "Đang nghe...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    // Nút dịch (giữ nguyên như trước)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            coroutineScope.launch {
                                viewModel.translate()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isTranslating && !isModelDownloading && sourceText.isNotBlank()
                    ) {
                        if (isModelDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang tải mô hình...")
                        } else if (isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang dịch...")
                        } else {
                            Text("Dịch")
                        }
                    }

                    // Kết quả dịch (giữ nguyên như trước)
                    if (translatedText.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = translatedText,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isSpeaking) {
                                                viewModel.stopSpeaking()
                                            } else {
                                                viewModel.speakTranslatedText()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = if (isSpeaking) "Dừng phát âm" else "Phát âm"
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.shareTranslation()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "Chia sẻ"
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
    @Composable
    fun HistoryScreen(
        history: List<TranslationHistoryItem>,
        onItemClick: (TranslationHistoryItem) -> Unit,
        onDeleteItem: (TranslationHistoryItem) -> Unit,
        onClearHistory: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lịch sử dịch",
                    style = MaterialTheme.typography.headlineSmall
                )

                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = onClearHistory
                    ) {
                        Text("Xóa tất cả")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có lịch sử dịch",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { item ->
                        HistoryItem(
                            item = item,
                            onClick = { onItemClick(item) },
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HistoryItem(
        item: TranslationHistoryItem,
        onClick: () -> Unit,
        onDelete: () -> Unit
    ) {
        val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${getLanguageDisplayName(item.sourceLanguage)} → ${
                            getLanguageDisplayName(
                                item.targetLanguage
                            )
                        }",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = dateFormat.format(item.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.sourceText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.translatedText,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            TranslateLanguage.ENGLISH -> "Tiếng Anh"
            TranslateLanguage.VIETNAMESE -> "Tiếng Việt"
            TranslateLanguage.CHINESE -> "Tiếng Trung"
            TranslateLanguage.JAPANESE -> "Tiếng Nhật"
            TranslateLanguage.KOREAN -> "Tiếng Hàn"
            TranslateLanguage.FRENCH -> "Tiếng Pháp"
            TranslateLanguage.GERMAN -> "Tiếng Đức"
            TranslateLanguage.RUSSIAN -> "Tiếng Nga"
            TranslateLanguage.SPANISH -> "Tiếng Tây Ban Nha"
            TranslateLanguage.ITALIAN -> "Tiếng Ý"
            else -> languageCode.replaceFirstChar { it.uppercase() }
        }
    }
}