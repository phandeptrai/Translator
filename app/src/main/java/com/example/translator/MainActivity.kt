package com.example.translator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.translator.ui.screen.TranslateScreen
import com.example.translator.ui.theme.TranslatorTheme
import com.example.translator.viewmodel.TranslateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TranslateViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TranslatorTheme {
                TranslateScreen(viewModel)
            }
        }
    }
}