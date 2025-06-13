package com.example.translator

import android.app.Application
import com.example.translator.di.networkModule
import org.koin.core.context.startKoin

class TranslatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(networkModule)
        }
    }
}