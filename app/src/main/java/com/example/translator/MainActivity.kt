package com.example.translator

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.translator.api.RetrofitClient
import com.example.translator.data.AppDatabase
import com.example.translator.data.Translation
import com.example.translator.TranslationService
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "translator-db").build()

        setContent {
            TranslatorApp(db, tts)
        }

        startService(Intent(this, TranslationService::class.java))
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}

@Composable
fun TranslatorApp(db: AppDatabase, tts: TextToSpeech) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(db, tts, navController) }
        composable("history") { HistoryScreen(db, navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(db: AppDatabase, tts: TextToSpeech, navController: androidx.navigation.NavController) {
    var inputText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var sourceLang by remember { mutableStateOf("en") }
    var targetLang by remember { mutableStateOf("vi") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Lỗi") },
            text = { Text("Dịch thất bại. Vui lòng thử lại.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ứng dụng Dịch") },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Xem lịch sử") },
                            onClick = {
                                navController.navigate("history")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa") },
                            onClick = {
                                inputText = ""
                                translatedText = ""
                                expanded = false
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Nhập văn bản") },
                modifier = Modifier.fillMaxWidth()
            )
            Row {
                TextField(
                    value = sourceLang,
                    onValueChange = { sourceLang = it },
                    label = { Text("Ngôn ngữ nguồn") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = targetLang,
                    onValueChange = { targetLang = it },
                    label = { Text("Ngôn ngữ đích") },
                    modifier = Modifier.weight(1f)
                )
            }
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.api.translate(
                            inputText,
                            sourceLang,
                            targetLang,
                            "YOUR_API_KEY"
                        )
                        translatedText = response.data.translations[0].translatedText
                        db.translationDao().insert(
                            Translation(
                                inputText = inputText,
                                translatedText = translatedText,
                                sourceLang = sourceLang,
                                targetLang = targetLang,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        Toast.makeText(context, "Đã lưu bản dịch", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        showDialog = true
                    }
                }
            }) {
                Text("Dịch")
            }
            Text("Kết quả: $translatedText")
            Button(onClick = { tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null) }) {
                Text("Phát âm")
            }
            Button(onClick = {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, translatedText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(intent, "Chia sẻ bản dịch"))
            }) {
                Text("Chia sẻ")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(db: AppDatabase, navController: androidx.navigation.NavController) {
    val translations by db.translationDao().getAllTranslations().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử dịch") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(translations) { translation ->
                Text("${translation.inputText} -> ${translation.translatedText} (${translation.sourceLang} sang ${translation.targetLang})")
            }
        }
    }
}