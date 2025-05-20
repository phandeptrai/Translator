package com.example.translator

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TranslationService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                Toast.makeText(applicationContext, "Đang kiểm tra cập nhật API...", Toast.LENGTH_SHORT).show()
                delay(60000) // Mô phỏng kiểm tra mỗi phút
            }
        }
        return START_STICKY
    }
}