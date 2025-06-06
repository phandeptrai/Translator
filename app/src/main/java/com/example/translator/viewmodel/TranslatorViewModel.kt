//package com.example.translator.viewmodel
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import androidx.compose.runtime.mutableStateOf
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModel
//import com.example.translator.voicetotext.SpeechRecognizerManager
//
//class TranslatorViewModel : ViewModel() {
//    // Thêm state mới
//    val isRecording = mutableStateOf(false)
//
//    // Thêm hàm xử lý thu âm
//    fun startVoiceInput(context: Context) {
//        if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.RECORD_AUDIO
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Xử lý khi không có quyền
//            return
//        }
//
//        isRecording.value = true
//        _sourceText.value = "" // Xóa text cũ
//
//        val speechRecognizerManager = SpeechRecognizerManager(
//            context = context,
//            onResult = { result ->
//                _sourceText.value = result
//                isRecording.value = false
//            },
//            onError = { error ->
//                isRecording.value = false
//                // Xử lý lỗi
//            }
//        )
//
//        speechRecognizerManager.startListening()
//    }
//
//    fun stopVoiceInput() {
//        isRecording.value = false
//        // Dừng thu âm nếu cần
//    }
//}