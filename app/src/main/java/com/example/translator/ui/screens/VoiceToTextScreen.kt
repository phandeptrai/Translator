package com.example.translator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.translator.ui.components.MicButton
import com.example.translator.voicetotext.SpeechRecognizerManager


@Composable
fun VoiceToTextScreen() {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }

    val speechRecognizerManager = remember {
        SpeechRecognizerManager(
            context = context,
            onResult = { result -> text = result },
            onError = { error -> text = "Error: $error" }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizerManager.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text, modifier = Modifier.padding(16.dp))
        MicButton(
            onStartListening = { speechRecognizerManager.startListening() },
            onStopListening = { speechRecognizerManager.stopListening() }
        )
    }
}