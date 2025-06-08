package com.example.translator.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
data class Translation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val timestamp: Long = System.currentTimeMillis()
) 