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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.translator.TranslatorViewModel
import com.example.translator.ui.components.LanguageSelector
import com.example.translator.ui.components.TranslationInput
import com.example.translator.ui.components.TranslationOutput
import com.example.translator.speechtotext.SpeechRecognizerManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TranslatorViewModel = viewModel(),
    onShowHistory: () -> Unit,
    onShowOfflineLanguages: () -> Unit
) {
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
    val downloadedLanguages by viewModel.downloadedLanguages.collectAsState()
    val supportedLanguages = remember { commonLanguages.map { it.code } }

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
                    IconButton(onClick = onShowOfflineLanguages) {
                        Icon(Icons.Default.Download, contentDescription = "Tải xuống ngôn ngữ offline")
                    }
                    IconButton(onClick = onShowHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Xem lịch sử"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LanguageSelector(
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                supportedLanguages = supportedLanguages,
                downloadedLanguages = downloadedLanguages,
                onSourceLanguageSelected = { language ->
                    coroutineScope.launch {
                        viewModel.setSourceLanguage(language)
                        showSourceLanguageMenu = false
                    }
                },
                onTargetLanguageSelected = { language ->
                    coroutineScope.launch {
                        viewModel.setTargetLanguage(language)
                        showTargetLanguageMenu = false
                    }
                },
                onSwapLanguages = {
                    coroutineScope.launch {
                        viewModel.swapLanguages()
                    }
                }
            )

            TranslationInput(
                sourceText = sourceText,
                onSourceTextChange = { viewModel.updateSourceText(it) },
                isRecording = isRecording,
                onRecordClick = {
                    if (isRecording) {
                        speechRecognizerManager.stopListening()
                        isRecording = false
                    } else {
                        speechRecognizerManager.startListening()
                        isRecording = true
                    }
                }
            )

            TranslationOutput(
                translatedText = translatedText,
                isTranslating = isTranslating,
                isModelDownloading = isModelDownloading,
                isSpeaking = isSpeaking,
                onSpeakClick = { viewModel.speakText() },
                onShareClick = { viewModel.shareTranslation() }
            )
        }
    }
} 