package com.example.translator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.translator.ui.screens.MainScreen
import com.example.translator.ui.screens.HistoryScreen
import com.example.translator.ui.theme.TranslatorTheme
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.translator.navigation.AppNavGraph

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object History : Screen("history")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAudioPermission()
        
        // Xử lý Intent từ tính năng dịch khi chia sẻ văn bản
        handleIntent(intent)
        
        setContent {
            TranslatorTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Xử lý Intent khi app đã mở và nhận Intent mới
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                // Lưu văn bản nhận được để truyền xuống ViewModel
                SharedTextManager.setText(sharedText)
            }
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
        }
    }
}

// Helper class để truyền văn bản từ Intent xuống ViewModel
object SharedTextManager {
    private var sharedText: String? = null
    
    fun setText(text: String) {
        sharedText = text
    }
    
    fun getText(): String? {
        val text = sharedText
        sharedText = null // Reset sau khi lấy
        return text
    }
}